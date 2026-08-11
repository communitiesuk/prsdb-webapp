package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// TODO PDJB-1238: remove this placeholder step once the companies house update journey exists. It renders a dead-end
//  TODO page (no continue button) so the corresponding check-your-answers change links don't 404. The todoComment
//  shown is supplied per branch where the step is wired into the journey map.
@JourneyFrameworkComponent
class UpdateDetailsTodoStepConfig : AbstractRequestableStepConfig<Complete, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: JourneyState) = emptyMap<String, Any?>()

    override fun chooseTemplate(state: JourneyState) = "forms/todoNoContinue"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class UpdateDetailsTodoStep(
    stepConfig: UpdateDetailsTodoStepConfig,
) : RequestableStep<Complete, NoInputFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "update-details-todo"
    }
}
