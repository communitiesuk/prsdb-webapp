package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskList

data class TaskListItemViewModel(
    val nameKey: String,
    val status: TaskStatusViewModel,
    val url: String? = null,
) {
    companion object {
        fun <T : StepId> fromTaskAndStatus(
            task: TaskList.Task<T>,
            status: TaskStatus,
        ): TaskListItemViewModel =
            TaskListItemViewModel(
                task.nameKey,
                TaskStatusViewModel.fromStatus(status),
                if (status == TaskStatus.CANNOT_START_YET) {
                    null
                } else {
                    task.startId.urlPathSegment
                },
            )
    }
}
