package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskListViewModelFactory
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class JourneyWithTaskList<T : StepId>(
    val journeyType: JourneyType,
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<T>(journeyType, validator, journeyDataService) {
    abstract val taskListFactory: TaskListViewModelFactory<T>
    abstract val taskListUrlSegment: String

    final override fun getUnreachableStepRedirect(journeyData: JourneyData) = "/${journeyType.urlPathSegment}/$taskListUrlSegment"

    fun populateModelAndGetTaskListViewName(model: Model): String {
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
        "registerProperty.title",
        "registerProperty.taskList.heading",
        "registerProperty.taskList.subtitle",
        "register-property-task",
        sections,
    ) { task, journeyData -> getTaskStatus(task, journeyData) }
}
