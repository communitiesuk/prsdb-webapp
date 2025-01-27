package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus

data class TaskStatusViewModel(
    val textKey: String,
    val tag: Boolean = false,
    val tagClass: String? = null,
    val cannotStart: Boolean = false,
) {
    companion object {
        fun fromStatus(status: TaskStatus): TaskStatusViewModel {
            when (status) {
                TaskStatus.CANNOT_START_YET -> return TaskStatusViewModel("taskList.status.cannotStartYet", cannotStart = true)
                TaskStatus.NOT_YET_STARTED -> return TaskStatusViewModel("taskList.status.notYetStarted", true, "govuk-tag--blue")
                TaskStatus.IN_PROGRESS -> return TaskStatusViewModel("taskList.status.inProgress", true, "govuk-tag--light-blue")
                TaskStatus.COMPLETED -> return TaskStatusViewModel("taskList.status.completed")
            }
        }
    }
}
