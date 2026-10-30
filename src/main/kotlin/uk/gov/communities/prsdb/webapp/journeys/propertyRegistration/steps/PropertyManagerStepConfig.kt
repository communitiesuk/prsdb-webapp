package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TemporaryPropertyManagerYesOrNoFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel

// TODO PDJB-1980: replace this placeholder with the real "Are you using a property manager?" page
//  (content, form model, validation and persistence). Rendered with the shared forms/todoWithRadios placeholder page.
@JourneyFrameworkComponent
class PropertyManagerStepConfig :
    AbstractRequestableStepConfig<YesOrNo, TemporaryPropertyManagerYesOrNoFormModel, JourneyState>() {
    override val formModelClass = TemporaryPropertyManagerYesOrNoFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldSetHeading" to "registerProperty.propertyManager.todo.usingPropertyManager.heading",
            "fieldName" to "answer",
            "radioOptions" to RadiosViewModel.yesOrNoRadios(),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState): YesOrNo? = getFormModelFromStateOrNull(state)?.toYesOrNo()
}

@JourneyFrameworkComponent
final class PropertyManagerStep(
    stepConfig: PropertyManagerStepConfig,
) : RequestableStep<YesOrNo, TemporaryPropertyManagerYesOrNoFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "property-manager"
    }
}
