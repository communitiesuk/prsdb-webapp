package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

@JourneyFrameworkComponent
class OrgAddressStepConfig : AbstractRequestableStepConfig<Complete, ManualAddressFormModel, JourneyState>() {
    override val formModelClass = ManualAddressFormModel::class

//  TODO: PDJB-1133 - Add in auto address lookup (maybe another config step)

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.manualAddress.organisationLandlordRegistration.fieldSetHeading",
            "fieldSetHint" to "forms.manualAddress.organisationLandlordRegistration.fieldSetHint",
            "addressLineOneLabel" to "forms.manualAddress.addressLineOne.label",
            "addressLineTwoLabel" to "forms.manualAddress.addressLineTwo.label",
            "townOrCityLabel" to "forms.manualAddress.townOrCity.label",
            "countyLabel" to "forms.manualAddress.county.label",
            "postcodeLabel" to "forms.manualAddress.postcode.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/manualAddressForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgAddressStep(
    stepConfig: OrgAddressStepConfig,
) : RequestableStep<Complete, ManualAddressFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-address"
    }
}
