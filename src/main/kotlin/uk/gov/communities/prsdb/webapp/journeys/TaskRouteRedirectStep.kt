package uk.gov.communities.prsdb.webapp.journeys

import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator.RedirectingStepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

// The single mode for a task-route landing step: it never renders, it only redirects.
enum class TaskRouteRedirectMode {
    REDIRECT,
}

// A URL-addressable step that redirects to a task's first step rather than rendering a page.
// TaskInitialiser instantiates one per routed task and initialises its next destination to the
// task's firstStep, so a request to a bare task route lands on the task's genuine first step -
// including when that first step is an internal (non-map) step. It follows the same "redirecting
// requestable step" pattern as CheckUserRoleStepConfig, so it is added to the routing map via the
// normal RequestableStep branch. Instantiated programmatically rather than as a Spring bean: the
// validator and form model are never dereferenced on the redirecting path.
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
