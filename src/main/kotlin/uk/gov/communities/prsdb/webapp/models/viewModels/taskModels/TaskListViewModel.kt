package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

data class TaskListViewModel(
    val title: String,
    val pageHeading: String,
    val subtitle: String,
    val rootTaskId: String,
    val taskSections: List<TaskSectionViewModel>,
)
