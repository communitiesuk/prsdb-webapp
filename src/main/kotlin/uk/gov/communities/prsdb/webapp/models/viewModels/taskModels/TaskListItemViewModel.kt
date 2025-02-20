package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.steps.StepId

data class TaskListItemViewModel(
    val nameKey: String,
    val status: TaskStatusViewModel,
    val url: String? = null,
) {
    companion object {
        fun <T : StepId> fromTaskDetails(
            nameKey: String,
            status: TaskStatus,
            initialStepId: T,
        ) = TaskListItemViewModel(
            nameKey,
            TaskStatusViewModel.fromStatus(status),
            if (status == TaskStatus.CANNOT_START_YET) {
                null
            } else {
                initialStepId.urlPathSegment
            },
        )
    }
}
