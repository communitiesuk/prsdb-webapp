package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.CORRESPONDENCE_ADDRESS
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CorrespondenceEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.CorrespondenceAddressTask
import uk.gov.communities.prsdb.webapp.services.CurrentUserService

class PropertyRegistrationJourneyFactoryTests {
    @Test
    fun `createJourneySteps treats the who-provides answer as an unknown checkable element when the delegate feature flag is disabled`() {
        val factory = factoryFor(checkingAnswersFor = WhoProvidesRentalDetailsStep.ROUTE_SEGMENT, delegateEnabled = false)

        val exception = assertThrows<IllegalStateException> { factory.createJourneySteps() }

        assertEquals("Unknown checkable element ${WhoProvidesRentalDetailsStep.ROUTE_SEGMENT}", exception.message)
    }

    // TODO PDJB-1733: Remove this test and correspondenceRoutes() when the CORRESPONDENCE_ADDRESS feature flag is removed
    @ParameterizedTest
    @MethodSource("correspondenceRoutes")
    fun `createJourneySteps treats contact answers as unknown checkable elements when the CORRESPONDENCE_ADDRESS flag is off`(
        checkingAnswersFor: String,
    ) {
        val factory = factoryFor(checkingAnswersFor, correspondenceEnabled = false)

        val exception = assertThrows<IllegalStateException> { factory.createJourneySteps() }

        assertEquals("Unknown checkable element $checkingAnswersFor", exception.message)
    }

    private fun factoryFor(
        checkingAnswersFor: String?,
        delegateEnabled: Boolean = false,
        correspondenceEnabled: Boolean = false,
    ): PropertyRegistrationJourneyFactory {
        val state = mock<PropertyRegistrationJourneyState> { on { this.checkingAnswersFor } doReturn checkingAnswersFor }
        val stateFactory = mock<ObjectFactory<PropertyRegistrationJourneyState>> { on { getObject() } doReturn state }
        val featureFlagManager =
            mock<FeatureFlagManager> {
                on { checkFeature(DELEGATE_TO_LETTING_AGENT) } doReturn delegateEnabled
                on { checkFeature(CORRESPONDENCE_ADDRESS) } doReturn correspondenceEnabled
            }
        val currentUserService = mock<CurrentUserService> { on { getCurrentEmail() } doReturn "original.landlord@example.com" }
        val paymentsStrategy = mock<PaymentsPropertyRegistrationStrategy>()
        return PropertyRegistrationJourneyFactory(stateFactory, featureFlagManager, currentUserService, paymentsStrategy)
    }

    companion object {
        @JvmStatic
        fun correspondenceRoutes() =
            listOf(
                CorrespondenceEmailStep.ROUTE_SEGMENT,
                "${CorrespondenceAddressTask.ROUTE_SEGMENT}/${LookupAddressStep.ROUTE_SEGMENT}",
            )
    }
}
