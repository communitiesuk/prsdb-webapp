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
    private val subtitleKey: String,
    private val rootId: String,
    private val sections: List<JourneySection<T>>,
    val getTaskStatus: (task: JourneyTask<T>, journeyData: JourneyData) -> TaskStatus,
) {
    fun getTaskListViewModel(journeyData: JourneyData): TaskListViewModel {
        val sectionViewModels =
            sections.mapNotNull { section ->
                section.headingKey?.let { headingKey ->
                    TaskSectionViewModel(
                        headingKey,
                        section.tasks.mapNotNull { task ->
                            task.nameKey?.let { nameKey ->
                                TaskListItemViewModel.fromTaskDetails(
                                    nameKey,
                                    getTaskStatus(task, journeyData),
                                    task.startingStepId,
                                )
                            }
                        },
                    )
                }
            }

        return TaskListViewModel(titleKey, headingKey, subtitleKey, rootId, sectionViewModels)
    }
}
