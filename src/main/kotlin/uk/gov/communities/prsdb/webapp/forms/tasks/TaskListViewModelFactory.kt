package uk.gov.communities.prsdb.webapp.forms.tasks

import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskSectionViewModel

class TaskListViewModelFactory<T : StepId>(
    private val titleKey: String,
    private val headingKey: String,
    private val subtitleKeys: List<String>,
    private val sections: List<JourneySection<T>>,
    private val numberSections: Boolean = true,
    val getTaskStatus: (task: JourneyTask<T>, journeyData: JourneyData) -> TaskStatus,
) {
    fun getTaskListViewModel(journeyData: JourneyData): TaskListViewModel {
        val sectionViewModels =
            sections.mapNotNull { section ->
                if (section.headingKey == null || section.sectionId == null) {
                    null
                } else {
                    TaskSectionViewModel(
                        section.headingKey,
                        section.sectionId,
                        section.tasks.mapNotNull { task ->
                            task.nameKey?.let { nameKey ->
                                TaskListItemViewModel.fromTaskDetails(
                                    nameKey,
                                    getTaskStatus(task, journeyData),
                                    task.hintKey,
                                    task.startingStepId,
                                )
                            }
                        },
                    )
                }
            }

        return TaskListViewModel(titleKey, headingKey, subtitleKeys, sectionViewModels, numberSections)
    }
}
