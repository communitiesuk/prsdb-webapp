package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_BLUE
import uk.gov.communities.prsdb.webapp.constants.TAG_COLOUR_LIGHT_BLUE
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus

data class TaskStatusViewModel(
    val textKey: String,
    val colour: String? = null,
    val isCannotStart: Boolean = false,
) {
    val isTag
        get() = colour != null

    companion object {
        fun fromStatus(status: TaskStatus): TaskStatusViewModel {
            when (status) {
                TaskStatus.CANNOT_START -> return TaskStatusViewModel(
                    "taskList.status.cannotStart",
                    isCannotStart = true,
                )

                TaskStatus.NOT_STARTED -> return TaskStatusViewModel(
                    "taskList.status.notStarted",
                    colour = TAG_COLOUR_BLUE,
                )

                TaskStatus.IN_PROGRESS -> return TaskStatusViewModel(
                    "taskList.status.inProgress",
                    colour = TAG_COLOUR_LIGHT_BLUE,
                )

                TaskStatus.COMPLETED -> return TaskStatusViewModel("taskList.status.completed")
            }
        }
    }
}
