package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

interface AddressState : AddressSearchState {
    val noAddressFoundStep: NoAddressFoundStep
    val selectAddressStep: SelectAddressStep
    val manualAddressStep: ManualAddressStep
    var isAddressAlreadyRegistered: Boolean?

    fun getMatchingAddress(address: String): AddressDataModel? = cachedAddresses?.singleOrNull { it.singleLineAddress == address }

    fun getManualAddressOrNull(): AddressDataModel? =
        manualAddressStep.formModelOrNull?.let { manualAddressData ->
            AddressDataModel.fromManualAddressData(
                addressLineOne = manualAddressData.notNullValue(ManualAddressFormModel::addressLineOne),
                addressLineTwo = manualAddressData.addressLineTwo,
                townOrCity = manualAddressData.notNullValue(ManualAddressFormModel::townOrCity),
                county = manualAddressData.county,
                postcode = manualAddressData.notNullValue(ManualAddressFormModel::postcode),
            )
        }

    fun getAddress(): AddressDataModel {
        val selectedAddress = selectAddressStep.formModelOrNull?.address?.let { getMatchingAddress(it) }
        return selectedAddress ?: getManualAddressOrNull() ?: throw NotNullFormModelValueIsNullException("No address found in AddressState")
    }
}
