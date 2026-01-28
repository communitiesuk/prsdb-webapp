package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states

import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.AlreadyRegisteredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalCouncilFormModel

interface PropertyRegistrationAddressState : AddressState {
    val alreadyRegisteredStep: AlreadyRegisteredStep
    val localCouncilStep: LocalCouncilStep

    override fun getManualAddressOrNull() =
        manualAddressStep.formModelOrNull?.let { manualAddressData ->
            localCouncilStep.formModelOrNull?.let { localCouncilData ->
                AddressDataModel.fromManualAddressData(
                    addressLineOne = manualAddressData.notNullValue(ManualAddressFormModel::addressLineOne),
                    addressLineTwo = manualAddressData.addressLineTwo,
                    townOrCity = manualAddressData.notNullValue(ManualAddressFormModel::townOrCity),
                    county = manualAddressData.county,
                    postcode = manualAddressData.notNullValue(ManualAddressFormModel::postcode),
                    localCouncilId = localCouncilData.notNullValue(SelectLocalCouncilFormModel::localCouncilId),
                )
            }
        }
}
