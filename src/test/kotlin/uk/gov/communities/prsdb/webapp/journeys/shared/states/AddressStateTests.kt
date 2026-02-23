package uk.gov.communities.prsdb.webapp.journeys.shared.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectFromListFormModel

class AddressStateTests {
    @Test
    fun `getMatchingAddress returns null when cachedAddresses is null`() {
        val state = buildTestAddressState(cachedAddresses = null)
        assertNull(state.getMatchingAddress("any address"))
    }

    @Test
    fun `getMatchingAddress returns null when cachedAddresses does not contain address`() {
        val state = buildTestAddressState(cachedAddresses = listOf(AddressDataModel("1 Test St, City, AB1 2CD")))
        assertNull(state.getMatchingAddress("any other address"))
    }

    @Test
    fun `getMatchingAddress returns address when cachedAddresses contains it`() {
        // Arrange
        val addressModel = AddressDataModel("1 Test St, City, AB1 2CD")
        val state = buildTestAddressState(cachedAddresses = listOf(addressModel))

        // Act & Assert
        assertEquals(addressModel, state.getMatchingAddress(addressModel.singleLineAddress))
    }

    @Test
    fun `getManualAddressOrNull returns null if manualAddressStep's form model is null`() {
        val state = buildTestAddressState(manualAddressFormModel = null)
        assertNull(state.getManualAddressOrNull())
    }

    @Test
    fun `getManualAddressOrNull throws if manualAddressStep's form model is invalid`() {
        val state =
            buildTestAddressState(
                manualAddressFormModel =
                    ManualAddressFormModel().apply {
                        addressLineOne = "Flat 1"
                        townOrCity = null
                        postcode = "ZZ1 1ZZ"
                    },
            )
        assertThrows(NotNullFormModelValueIsNullException::class.java) { state.getManualAddressOrNull() }
    }

    @Test
    fun `getManualAddressOrNull returns address if manualAddressStep's form model is valid`() {
        // Arrange
        val manualAddressFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "Flat 1"
                townOrCity = "Town"
                postcode = "ZZ1 1ZZ"
                addressLineTwo = "Building"
                county = "County"
            }
        val state = buildTestAddressState(manualAddressFormModel = manualAddressFormModel)

        // Act & Assert
        val expectedResult =
            AddressDataModel.fromManualAddressData(
                manualAddressFormModel.addressLineOne!!,
                manualAddressFormModel.townOrCity!!,
                manualAddressFormModel.postcode!!,
                manualAddressFormModel.addressLineTwo,
                manualAddressFormModel.county,
            )
        assertEquals(expectedResult, state.getManualAddressOrNull())
    }

    @Test
    fun `getAddress returns selected address if present`() {
        // Arrange
        val address = AddressDataModel("1 Test St, City, AB1 2CD")
        val selectForm = SelectFromListFormModel().apply { this.selectedOption = address.singleLineAddress }
        val state = buildTestAddressState(selectAddressFormModel = selectForm, cachedAddresses = listOf(address))

        // Act & Assert
        assertEquals(address, state.getAddress())
    }

    @Test
    fun `getAddress returns manual address if present and there's no selected address`() {
        // Arrange
        val manualAddressFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "Flat 1"
                townOrCity = "Town"
                postcode = "ZZ1 1ZZ"
            }
        val state = buildTestAddressState(selectAddressFormModel = null, manualAddressFormModel = manualAddressFormModel)

        // Act & Assert
        val expectedResult =
            AddressDataModel.fromManualAddressData(
                manualAddressFormModel.addressLineOne!!,
                manualAddressFormModel.townOrCity!!,
                manualAddressFormModel.postcode!!,
            )
        assertEquals(expectedResult, state.getAddress())
    }

    @Test
    fun `getAddress throws if neither selected nor manual address present`() {
        val state = buildTestAddressState(selectAddressFormModel = null, manualAddressFormModel = null)
        assertThrows(NotNullFormModelValueIsNullException::class.java) { state.getAddress() }
    }

    private fun buildTestAddressState(
        selectAddressFormModel: SelectFromListFormModel? = null,
        manualAddressFormModel: ManualAddressFormModel? = null,
        cachedAddresses: List<AddressDataModel>? = null,
    ): AddressState =
        object : AbstractJourneyState(journeyStateService = mock()), AddressState {
            override val lookupAddressStep = mock<LookupAddressStep>()

            override val noAddressFoundStep = mock<NoAddressFoundStep>()

            override val selectAddressStep =
                mock<SelectAddressStep>().apply {
                    whenever(this.formModelOrNull).thenReturn(selectAddressFormModel)
                }

            override val manualAddressStep =
                mock<ManualAddressStep>().apply {
                    whenever(this.formModelOrNull).thenReturn(manualAddressFormModel)
                }

            override var cachedAddresses: List<AddressDataModel>? = cachedAddresses

            override var isAddressAlreadyRegistered: Boolean? = null
        }
}
