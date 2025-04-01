package uk.gov.communities.prsdb.webapp.models.viewModels.taskModels

data class TaskListViewModel(
    val title: String,
    val pageHeading: String,
    val subtitles: List<String>,
    val taskSections: List<TaskSectionViewModel>,
    val numberSections: Boolean = true,
)
