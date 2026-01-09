package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

interface AddressState : JourneyState {
    val lookupStep: LookupAddressStep
    val selectAddressStep: SelectAddressStep
    val alreadyRegisteredStep: AlreadyRegisteredStep
    val noAddressFoundStep: NoAddressFoundStep
    val manualAddressStep: ManualAddressStep
    val localCouncilStep: LocalCouncilStep
    var cachedAddresses: List<AddressDataModel>?
    var isAddressAlreadyRegistered: Boolean?

    fun getMatchingAddress(address: String): AddressDataModel? = cachedAddresses?.singleOrNull { it.singleLineAddress == address }

    fun getAddressOrNull(): AddressDataModel? {
        val selectedAddress = selectAddressStep.formModelOrNull?.address?.let { getMatchingAddress(it) }
        return selectedAddress ?: AddressDataModel.fromManualAddressDataOrNull()
    }

    fun getAddress(): AddressDataModel =
        getAddressOrNull() ?: throw NotNullFormModelValueIsNullException("No address found in AddressState")

    private fun AddressDataModel.Companion.fromManualAddressDataOrNull() =
        manualAddressStep.formModelOrNull?.let { manualAddressData ->
            localCouncilStep.formModelOrNull?.let { localCouncilData ->
                AddressDataModel.fromManualAddressData(
                    addressLineOne = manualAddressData.notNullValue(ManualAddressFormModel::addressLineOne),
                    addressLineTwo = manualAddressData.addressLineTwo,
                    townOrCity = manualAddressData.notNullValue(ManualAddressFormModel::townOrCity),
                    county = manualAddressData.county,
                    postcode = manualAddressData.notNullValue(ManualAddressFormModel::postcode),
                    localCouncilId = localCouncilData.localCouncilId,
                )
            }
        }
}
