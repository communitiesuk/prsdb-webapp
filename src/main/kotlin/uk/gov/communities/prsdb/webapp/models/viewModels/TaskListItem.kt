package uk.gov.communities.prsdb.webapp.models.viewModels

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus

data class TaskListItem(
    val name: String,
    val status: TaskStatus,
    val url: String? = null,
)
