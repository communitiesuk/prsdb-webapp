package uk.gov.communities.prsdb.webapp.models.viewModels

data class TaskListItem(
    val nameKey: String,
    val status: TaskStatusViewModel,
    val url: String? = null,
)
