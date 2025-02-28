package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskListViewModelFactory
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class JourneyWithTaskList<T : StepId>(
    journeyType: JourneyType,
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<T>(journeyType, validator, journeyDataService) {
    abstract val taskListFactory: TaskListViewModelFactory<T>
    abstract val taskListUrlSegment: String

    final override fun getUnreachableStepRedirect(journeyData: JourneyData) = taskListUrlSegment

    fun populateModelAndGetTaskListViewName(
        model: Model,
        journeyDataKey: String = journeyType.name,
    ): String {
        journeyDataService.journeyDataKey = journeyDataKey
        val journeyData = journeyDataService.getJourneyDataFromSession()
        model.addAttribute("taskListViewModel", taskListFactory.getTaskListViewModel(journeyData))
        return "taskList"
    }

    protected fun getTaskListViewModelFactory(
        titleKey: String,
        headingKey: String,
        subtitleKey: String,
        rootId: String,
    ) = TaskListViewModelFactory(
        titleKey,
        headingKey,
        subtitleKey,
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
