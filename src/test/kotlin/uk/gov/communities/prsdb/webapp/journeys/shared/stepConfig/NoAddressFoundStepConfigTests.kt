package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import uk.gov.communities.prsdb.webapp.constants.MANUAL_ADDRESS_CHOSEN
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState

class NoAddressFoundStepConfigTests {
    @Test
    fun `afterStepDataIsAdded caches manual address selection`() {
        val state = mock<AddressState>()

        NoAddressFoundStepConfig().afterStepDataIsAdded(state)

        verify(state).cachedSelectedAddress = MANUAL_ADDRESS_CHOSEN
    }
}
