package uk.gov.communities.prsdb.webapp.models.viewModels

data class TaskSectionViewModel(
    val headingKey: String,
    val tasks: List<TaskListItemViewModel>,
)
