package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

@Scope("prototype")
@PrsdbWebComponent
class ManualAddressStepConfig : AbstractGenericStepConfig<Complete, ManualAddressFormModel, JourneyState>() {
    override val formModelClass = ManualAddressFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.manualAddress.propertyRegistration.fieldSetHeading",
            "fieldSetHint" to "forms.manualAddress.fieldSetHint",
            "addressLineOneLabel" to "forms.manualAddress.addressLineOne.label",
            "addressLineTwoLabel" to "forms.manualAddress.addressLineTwo.label",
            "townOrCityLabel" to "forms.manualAddress.townOrCity.label",
            "countyLabel" to "forms.manualAddress.county.label",
            "postcodeLabel" to "forms.manualAddress.postcode.label",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/manualAddressForm"

    override fun mode(state: JourneyState) = Complete.COMPLETE
}

@Scope("prototype")
@PrsdbWebComponent
final class ManualAddressStep(
    stepConfig: ManualAddressStepConfig,
) : RequestableStep<Complete, ManualAddressFormModel, JourneyState>(stepConfig)
