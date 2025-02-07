package uk.gov.communities.prsdb.webapp.forms.tasks

import org.springframework.stereotype.Component
import org.springframework.validation.Validator
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.forms.journeys.PropertyRegistrationJourney
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.TaskStatusViewModel
import uk.gov.communities.prsdb.webapp.services.JourneyDataService

@Component
class CheckAndSubmitPropertiesTaskList(
    propertyRegistrationJourney: PropertyRegistrationJourney,
    journeyDataService: JourneyDataService,
    validator: Validator,
) : TaskList<RegisterPropertyStepId>(propertyRegistrationJourney, journeyDataService, validator) {
    override val taskList: List<Task<RegisterPropertyStepId>> =
        listOf(
            Task(
                "registerProperty.taskList.checkAndSubmit.checkAnswers",
                RegisterPropertyStepId.CheckAnswers,
                null,
            ),
        )
    // TODO PRSD-593: Pay for your properties

    override fun getTaskListViewModels() =
        super.getTaskListViewModels() +
            TaskListItemViewModel(
                "registerProperty.taskList.checkAndSubmit.payForYourProperties",
                TaskStatusViewModel.fromStatus(TaskStatus.CANNOT_START_YET),
            )
}
