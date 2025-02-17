package uk.gov.communities.prsdb.webapp.forms.journeys

import org.springframework.ui.Model
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.forms.steps.StepId
import uk.gov.communities.prsdb.webapp.forms.tasks.TaskListPage
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

abstract class JourneyWithTaskList<T : StepId>(
    val journeyType: JourneyType,
    validator: Validator,
    journeyDataService: JourneyDataService,
) : Journey<T>(journeyType, validator, journeyDataService) {
    abstract val taskListPage: TaskListPage<T>
    abstract val taskListUrlSegment: String

    final override val unreachableStepRedirect
        get() = "/${journeyType.urlPathSegment}/$taskListUrlSegment"

    fun populateModelAndGetTaskListViewName(model: Model): String {
        val journeyData = journeyDataService.getJourneyDataFromSession()
        return taskListPage?.populateModelAndGetTaskListViewName(model, journeyData) ?: "error/500"
    }
}
