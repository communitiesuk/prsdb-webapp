package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

data class TaskSectionViewModel(
    val headingKey: String,
    val sectionId: String,
    val tasks: List<TaskListItemViewModel>,
)
