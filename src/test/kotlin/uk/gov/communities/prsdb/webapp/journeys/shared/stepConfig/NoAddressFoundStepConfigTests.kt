package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import kotlin.test.assertEquals

class NoAddressFoundStepConfigTests {
    @Test
    fun `getStepSpecificContent overrides the page title`() {
        val lookupAddressStep = mock<LookupAddressStep>()
        whenever(lookupAddressStep.formModel).thenReturn(
            LookupAddressFormModel().apply {
                postcode = "AB1 2CD"
                houseNameOrNumber = "1"
            },
        )
        whenever(lookupAddressStep.currentJourneyId).thenReturn("journey-id")
        val state = mock<AddressState>()
        whenever(state.lookupAddressStep).thenReturn(lookupAddressStep)

        val content = NoAddressFoundStepConfig().getStepSpecificContent(state)

        assertEquals("addressForms.noAddressFound.pageTitle", content["pageTitleOverride"])
    }

    @Test
    fun `afterStepDataIsAdded caches manual address selection`() {
        val state = mock<AddressState>()

        NoAddressFoundStepConfig().afterStepDataIsAdded(state)

        verify(state).cachedSelectedAddress = MANUAL_ADDRESS_CHOSEN
    }
}
