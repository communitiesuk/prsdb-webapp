package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.steps.StepId

data class TaskListItemViewModel(
    val nameKey: String,
    val status: TaskStatusViewModel,
    val hintKey: String? = null,
    val url: String? = null,
) {
    companion object {
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
