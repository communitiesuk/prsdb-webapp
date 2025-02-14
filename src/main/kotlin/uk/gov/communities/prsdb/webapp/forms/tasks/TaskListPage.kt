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
    val titleKey: String,
    val headingKey: String,
    val subtitleKey: String,
    val rootId: String,
    val sections: List<JourneySection<T>>,
    val getTaskStatus: (task: JourneyTask<T>, journeyData: JourneyData) -> TaskStatus,
) {
    fun populateModelAndGetTemplateName(
        model: Model,
        journeyData: JourneyData,
    ): String {
        val sectionViewModels =
            sections.mapNotNull { section ->
                section.headingKey?.let {
                    TaskSectionViewModel(
                        it,
                        section.tasks.mapNotNull { task ->
                            task.nameKey?.let {
                                TaskListItemViewModel.fromTaskDetails(
                                    it,
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
