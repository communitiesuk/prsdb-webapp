package uk.gov.communities.prsdb.webapp.forms.tasks

import org.springframework.ui.Model
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyData
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneySection
import uk.gov.communities.prsdb.webapp.forms.journeys.JourneyTask
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskSectionViewModel

class TaskListPage<T : StepId>(
    private val titleKey: String,
    private val headingKey: String,
    private val subtitleKey: String,
    private val rootId: String,
    private val sections: List<JourneySection<T>>,
    val getTaskStatus: (task: JourneyTask<T>, journeyData: JourneyData) -> TaskStatus,
) {
    fun populateModelAndGetTaskListViewName(
        model: Model,
        journeyData: JourneyData,
    ): String {
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
        model.addAttribute("title", titleKey)
        model.addAttribute("pageHeading", headingKey)
        model.addAttribute("subtitle", subtitleKey)
        model.addAttribute("rootTaskId", rootId)
        model.addAttribute("taskSections", sectionViewModels)
        return "registerPropertyTaskList"
    }
}
