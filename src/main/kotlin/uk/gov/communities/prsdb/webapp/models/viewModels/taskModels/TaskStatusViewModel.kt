package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus

data class TaskStatusViewModel(
    val textKey: String,
    val tagClass: String? = null,
    val isCannotStart: Boolean = false,
) {
    val isTag
        get() = tagClass != null

    companion object {
        fun fromStatus(status: TaskStatus): TaskStatusViewModel {
            when (status) {
                TaskStatus.CANNOT_START -> return TaskStatusViewModel(
                    "taskList.status.cannotStart",
                    isCannotStart = true,
                )

                TaskStatus.NOT_STARTED -> return TaskStatusViewModel(
                    "taskList.status.notStarted",
                    tagClass = "govuk-tag--blue",
                )

                TaskStatus.IN_PROGRESS -> return TaskStatusViewModel(
                    "taskList.status.inProgress",
                    tagClass = "govuk-tag--light-blue",
                )

                TaskStatus.COMPLETED -> return TaskStatusViewModel("taskList.status.completed")
            }
        }
    }
}
