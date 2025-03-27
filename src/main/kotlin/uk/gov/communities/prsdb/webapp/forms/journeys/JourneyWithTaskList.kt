package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.TASK_LIST_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.JourneyData
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.JourneyTask
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskListViewModelFactory
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class JourneyWithTaskList<T : StepId>(
    journeyType: JourneyType,
    initialStepId: T,
    validator: Validator,
    journeyDataService: JourneyDataService,
    taskListUrlSegment: String = TASK_LIST_PATH_SEGMENT,
) : Journey<T>(journeyType, initialStepId, validator, journeyDataService) {
    protected abstract val taskListFactory: TaskListViewModelFactory<T>

    override val unreachableStepRedirect = taskListUrlSegment

    fun populateModelAndGetTaskListViewName(model: Model): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        model.addAttribute("taskListViewModel", taskListFactory.getTaskListViewModel(journeyData))
        return "taskList"
    }

    protected fun getTaskListViewModelFactory(
        titleKey: String,
        headingKey: String,
        subtitleKeys: List<String>,
        rootId: String,
    ) = TaskListViewModelFactory(
        titleKey,
        headingKey,
        subtitleKeys,
        rootId,
        sections,
    ) { task, journeyData -> getTaskStatus(task, journeyData) }

    private fun getTaskStatus(
        task: JourneyTask<T>,
        journeyData: JourneyData,
    ): TaskStatus {
        val canTaskBeStarted = isStepReachable(task.steps.single { it.id == task.startingStepId })
        return task.getTaskStatus(journeyData, validator, canTaskBeStarted)
    }
}
