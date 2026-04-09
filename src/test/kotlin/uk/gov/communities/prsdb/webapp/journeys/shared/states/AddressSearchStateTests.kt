package uk.gov.communities.prsdb.webapp.journeys.shared.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.kotlin.mock
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel

class AddressSearchStateTests {
    @Test
    fun `getMatchingAddress returns null when cachedAddresses is null`() {
        val state = buildTestAddressSearchState(cachedAddresses = null)
        assertNull(state.getMatchingAddress("any address"))
    }

    @Test
    fun `getMatchingAddress returns null when cachedAddresses does not contain address`() {
        val state = buildTestAddressSearchState(cachedAddresses = listOf(AddressDataModel("1 Test St, City, AB1 2CD")))
        assertNull(state.getMatchingAddress("any other address"))
    }

    @Test
    fun `getMatchingAddress returns address when cachedAddresses contains it`() {
        // Arrange
        val addressModel = AddressDataModel("1 Test St, City, AB1 2CD")
        val state = buildTestAddressSearchState(cachedAddresses = listOf(addressModel))

        // Act & Assert
        assertEquals(addressModel, state.getMatchingAddress(addressModel.singleLineAddress))
    }

    private fun buildTestAddressSearchState(cachedAddresses: List<AddressDataModel>? = null): AddressSearchState =
        object : AbstractJourneyState(journeyStateService = mock()), AddressSearchState {
            override val lookupAddressStep = mock<LookupAddressStep>()
            override var cachedAddresses: List<AddressDataModel>? = cachedAddresses
        }
}
