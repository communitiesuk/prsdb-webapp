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
class PropertyRegistrationTaskList(
    propertyRegistrationJourney: PropertyRegistrationJourney,
    journeyDataService: JourneyDataService,
    validator: Validator,
) : TaskList<RegisterPropertyStepId>(propertyRegistrationJourney, journeyDataService, validator) {
    override val taskList: List<TaskListItemDataModel<RegisterPropertyStepId>>
        get() =
            listOf(
                TaskListItemDataModel(
                    "registerProperty.taskList.register.addAddress",
                    RegisterPropertyStepId.LookupAddress,
                    RegisterPropertyStepId.PropertyType,
                ),
                TaskListItemDataModel(
                    "registerProperty.taskList.register.selectType",
                    RegisterPropertyStepId.PropertyType,
                    RegisterPropertyStepId.OwnershipType,
                ),
                TaskListItemDataModel(
                    "registerProperty.taskList.register.selectOwnership",
                    RegisterPropertyStepId.OwnershipType,
                    RegisterPropertyStepId.LicensingType,
                ),
                TaskListItemDataModel(
                    "registerProperty.taskList.register.addLicensing",
                    RegisterPropertyStepId.LicensingType,
                    RegisterPropertyStepId.Occupancy,
                ),
                TaskListItemDataModel(
                    "registerProperty.taskList.register.addTenancyInfo",
                    RegisterPropertyStepId.Occupancy,
                    RegisterPropertyStepId.LandlordType,
                ),
                TaskListItemDataModel(
                    "registerProperty.taskList.register.selectOperation",
                    RegisterPropertyStepId.LandlordType,
                    RegisterPropertyStepId.CheckAnswers,
                ),
            )
    // TODO PRSD-587 Add Interested parties

    override fun getTaskListViewModels(): List<TaskListItemViewModel> =
        super.getTaskListViewModels() +
            TaskListItemViewModel(
                "registerProperty.taskList.register.addInterestedParties",
                TaskStatusViewModel.fromStatus(TaskStatus.CANNOT_START_YET),
            )
}
