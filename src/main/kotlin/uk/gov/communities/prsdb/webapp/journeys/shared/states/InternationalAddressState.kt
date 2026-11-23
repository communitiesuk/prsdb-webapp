package uk.gov.communities.prsdb.webapp.journeys.shared.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

interface InternationalAddressState : JourneyState {
    val addressInUkStep: AddressInUkStep
    val internationalManualAddressStep: ManualAddressStep
    val addressTask: AddressTask

    fun addressInUk(): Boolean? = addressInUkStep.formModelOrNull?.addressInUk

    // The screener answer picks the branch, so an answer left over from the other branch is never returned
    fun getAddress(): AddressDataModel =
        when (addressInUk()) {
            true -> addressTask.getAddress()
            false -> getInternationalManualAddressOrNull()
            null -> null
        } ?: throw NotNullFormModelValueIsNullException("No address found in InternationalAddressState")

    private fun getInternationalManualAddressOrNull(): AddressDataModel? =
        internationalManualAddressStep.formModelOrNull?.let { manualAddressData ->
            AddressDataModel
                .fromManualAddressData(
                    addressLineOne = manualAddressData.notNullValue(ManualAddressFormModel::addressLineOne),
                    addressLineTwo = manualAddressData.addressLineTwo,
                    townOrCity = manualAddressData.notNullValue(ManualAddressFormModel::townOrCity),
                    county = manualAddressData.county,
                    postcode = manualAddressData.notNullValue(ManualAddressFormModel::postcode),
                ).copy(isInternational = true)
        }
}
