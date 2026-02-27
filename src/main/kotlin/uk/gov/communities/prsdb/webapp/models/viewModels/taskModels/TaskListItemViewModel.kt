package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.Task

data class TaskListItemViewModel(
    val nameKey: String,
    val status: TaskStatusViewModel,
    val hintKey: String? = null,
    val url: String? = null,
) {
    companion object {
        fun fromTask(
            nameKey: String,
            task: Task<*>,
            hintKey: String? = null,
        ): TaskListItemViewModel =
            TaskListItemViewModel(
                nameKey,
                TaskStatusViewModel.fromStatus(task.taskStatus()),
                hintKey,
                Destination(task.firstVisitableStep).toUrlStringOrNull(),
            )

        fun fromStep(
            nameKey: String,
            singleStepTask: RequestableStep<*, *, *>,
            hintKey: String? = null,
        ): TaskListItemViewModel =
            TaskListItemViewModel(
                nameKey,
                TaskStatusViewModel.fromStatus(singleStepTask.taskStatus()),
                hintKey,
                Destination(singleStepTask).toUrlStringOrNull(),
            )

        fun <T : StepId> fromTaskDetails(
            nameKey: String,
            status: TaskStatus,
            hintKey: String? = null,
            initialStepId: T,
        ) = TaskListItemViewModel(
            nameKey,
            TaskStatusViewModel.fromStatus(status),
            hintKey,
            if (status == TaskStatus.CANNOT_START) {
                null
            } else {
                initialStepId.urlPathSegment
            },
        )
    }
}

fun RequestableStep<*, *, *>.taskStatus(): TaskStatus =
    when {
        this.outcome != null -> TaskStatus.COMPLETED
        this.isStepReachable -> TaskStatus.NOT_STARTED
        else -> TaskStatus.CANNOT_START
    }
