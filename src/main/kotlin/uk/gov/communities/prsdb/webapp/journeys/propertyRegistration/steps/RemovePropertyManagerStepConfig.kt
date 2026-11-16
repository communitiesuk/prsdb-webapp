package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyManagerState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.RemovePropertyManagerFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

@JourneyFrameworkComponent
class RemovePropertyManagerStepConfig :
    AbstractRequestableStepConfig<YesOrNo, RemovePropertyManagerFormModel, PropertyManagerState>() {
    override val formModelClass = RemovePropertyManagerFormModel::class

    override fun getStepSpecificContent(state: PropertyManagerState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.propertyManager.remove.fieldSetHeading",
            "fieldSetHint" to "registerProperty.propertyManager.remove.fieldSetHint",
            "optionalFieldSetHeadingParam" to state.propertyManagerDetailsStep.formModel.name,
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            "submitButtonTextKey" to "forms.buttons.saveAndContinue",
            "showSectionHeader" to true,
        )

    override fun chooseTemplate(state: PropertyManagerState) = "forms/areYouSureForm"

    override fun mode(state: PropertyManagerState): YesOrNo =
        if (state.propertyManagerDetailsStep.formModelOrNull == null) YesOrNo.YES else YesOrNo.NO

    override fun afterStepDataIsAdded(state: PropertyManagerState) {
        if (getFormModelFromState(state).wantsToProceed == true) {
            state.propertyManagerDetailsStep.clearFormData()
        }
        state.removePropertyManagerStep.clearFormData()
    }
}

@JourneyFrameworkComponent
final class RemovePropertyManagerStep(
    stepConfig: RemovePropertyManagerStepConfig,
) : RequestableStep<YesOrNo, RemovePropertyManagerFormModel, PropertyManagerState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-property-manager"
    }
}
