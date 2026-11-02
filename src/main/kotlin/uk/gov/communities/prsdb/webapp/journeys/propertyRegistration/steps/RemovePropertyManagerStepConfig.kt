package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.PropertyManagerState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TemporaryPropertyManagerYesOrNoFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

// TODO PDJB-1983: replace this placeholder with the real "Are you sure you want to remove (property manager)?" page
//  (content, form model, validation and cancel link). Rendered with the shared forms/todoWithRadios placeholder page.
@JourneyFrameworkComponent
class RemovePropertyManagerStepConfig :
    AbstractRequestableStepConfig<YesOrNo, TemporaryPropertyManagerYesOrNoFormModel, PropertyManagerState>() {
    override val formModelClass = TemporaryPropertyManagerYesOrNoFormModel::class

    override fun getStepSpecificContent(state: PropertyManagerState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.propertyManager.todo.removePropertyManager.heading",
            "fieldName" to "answer",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: PropertyManagerState) = "forms/todoWithRadios"

    override fun mode(state: PropertyManagerState): YesOrNo? = getFormModelFromStateOrNull(state)?.toYesOrNo()

    override fun afterStepDataIsAdded(state: PropertyManagerState) {
        if (mode(state) == YesOrNo.YES) {
            state.propertyManagerDetailsStep.clearFormData()
        }
    }
}

@JourneyFrameworkComponent
final class RemovePropertyManagerStep(
    stepConfig: RemovePropertyManagerStepConfig,
) : RequestableStep<YesOrNo, TemporaryPropertyManagerYesOrNoFormModel, PropertyManagerState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "remove-property-manager"
    }
}
