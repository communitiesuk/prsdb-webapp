package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.validation.Validator
import org.springframework.web.servlet.ModelAndView
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
    protected val taskListUrlSegment: String = TASK_LIST_PATH_SEGMENT,
) : Journey<T>(journeyType, initialStepId, validator, journeyDataService) {
    protected abstract val taskListFactory: TaskListViewModelFactory<T>

    override val unreachableStepRedirect = taskListUrlSegment

    fun getModelAndViewForTaskList(): ModelAndView {
        val model = mapOf("taskListViewModel" to taskListFactory.getTaskListViewModel(last().filteredJourneyData))
        return ModelAndView("taskList", model)
    }

    protected fun getTaskListViewModelFactory(
        titleKey: String,
        headingKey: String,
        subtitleKeys: List<String>,
        numberSections: Boolean = true,
        backUrl: String? = null,
    ) = TaskListViewModelFactory(
        titleKey,
        headingKey,
        subtitleKeys,
        sections,
        numberSections,
        backUrl,
    ) { task, filteredJourneyData -> getTaskStatus(task, filteredJourneyData) }

    private fun getTaskStatus(
        task: JourneyTask<T>,
        filteredJourneyData: JourneyData,
    ): TaskStatus {
        val canTaskBeStarted = isStepReachable(task.steps.single { it.id == task.startingStepId })
        return task.getTaskStatus(filteredJourneyData, validator, canTaskBeStarted)
    }
}
