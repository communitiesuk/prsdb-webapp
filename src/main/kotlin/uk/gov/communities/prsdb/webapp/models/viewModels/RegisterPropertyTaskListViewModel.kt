package uk.gov.communities.prsdb.webapp.models.viewModels

data class RegisterPropertyTaskListViewModel(
    val registerTasks: List<TaskListItemViewModel>,
    val checkAndSubmitTasks: List<TaskListItemViewModel>,
)
