package uk.gov.communities.prsdb.webapp.models.viewModels

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
                TaskStatus.CANNOT_START_YET -> return TaskStatusViewModel("taskList.status.cannotStartYet", isCannotStart = true)
                TaskStatus.NOT_YET_STARTED -> return TaskStatusViewModel("taskList.status.notYetStarted", tagClass = "govuk-tag--blue")
                TaskStatus.IN_PROGRESS -> return TaskStatusViewModel("taskList.status.inProgress", tagClass = "govuk-tag--light-blue")
                TaskStatus.COMPLETED -> return TaskStatusViewModel("taskList.status.completed")
            }
        }
    }
}
