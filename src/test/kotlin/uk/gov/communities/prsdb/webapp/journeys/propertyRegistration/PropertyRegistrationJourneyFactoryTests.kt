package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep

class PropertyRegistrationJourneyFactoryTests {
    @Test
    fun `createJourneySteps treats the who-provides answer as an unknown checkable element when the delegate feature flag is disabled`() {
        val factory = factoryFor(checkingAnswersFor = WhoProvidesRentalDetailsStep.ROUTE_SEGMENT, delegateEnabled = false)

        val exception = assertThrows<IllegalStateException> { factory.createJourneySteps() }

        assertEquals("Unknown checkable element ${WhoProvidesRentalDetailsStep.ROUTE_SEGMENT}", exception.message)
    }

    private fun factoryFor(
        checkingAnswersFor: String?,
        delegateEnabled: Boolean,
    ): PropertyRegistrationJourneyFactory {
        val state = mock<PropertyRegistrationJourneyState> { on { this.checkingAnswersFor } doReturn checkingAnswersFor }
        val stateFactory = mock<ObjectFactory<PropertyRegistrationJourneyState>> { on { getObject() } doReturn state }
        val featureFlagManager = mock<FeatureFlagManager> { on { checkFeature(DELEGATE_TO_LETTING_AGENT) } doReturn delegateEnabled }
        return PropertyRegistrationJourneyFactory(stateFactory, featureFlagManager)
    }
}
