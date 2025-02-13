package uk.gov.communities.prsdb.webapp.models.viewModels

data class TaskSectionViewModel(
    val sectionHeadingKey: String,
    val sectionNumber: Int,
    val tasks: List<TaskListItemViewModel>,
)
