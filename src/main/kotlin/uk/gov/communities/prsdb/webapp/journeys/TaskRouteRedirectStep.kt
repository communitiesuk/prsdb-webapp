package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

enum class TaskRouteRedirectMode {
    REDIRECT,
}

class TaskRouteRedirectStepConfig : AbstractRequestableStepConfig<TaskRouteRedirectMode, NoInputFormModel, JourneyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepLifecycleOrchestrator(journeyStep: JourneyStep<*, *, *>) = RedirectingStepLifecycleOrchestrator(journeyStep)

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> = emptyMap()

    override fun chooseTemplate(state: JourneyState): String = ""

    override fun mode(state: JourneyState): TaskRouteRedirectMode = TaskRouteRedirectMode.REDIRECT
}

class TaskRouteRedirectStep(
    stepConfig: TaskRouteRedirectStepConfig,
) : JourneyStep.RequestableStep<TaskRouteRedirectMode, NoInputFormModel, JourneyState>(stepConfig)
