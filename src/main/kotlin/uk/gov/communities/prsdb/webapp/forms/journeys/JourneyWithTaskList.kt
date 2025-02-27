package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
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

    fun initialiseJourneyDataIfNotInitialised(
        principalName: String,
        journeyDataKey: String = journeyType.name,
    ) {
        journeyDataService.journeyDataKey = journeyDataKey
        val data = journeyDataService.getJourneyDataFromSession()
        if (data.isEmpty()) {
            /* TODO PRSD-589 Currently this looks the context up from the database,
                takes the id, then passes the id to another method which retrieves it
                from the database. When this is reworked, we should just pass the whole
                context to an overload of journeyDataService.loadJourneyDataIntoSession().*/
            val contextId = journeyDataService.getContextId(principalName, journeyType)
            if (contextId != null) {
                journeyDataService.loadJourneyDataIntoSession(contextId)
            }
        }
    }

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
        "registerProperty.title",
        "registerProperty.taskList.heading",
        "registerProperty.taskList.subtitle",
        "register-property-task",
        sections,
    ) { task, journeyData -> getTaskStatus(task, journeyData) }
}
