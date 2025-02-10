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
class RegisterPropertiesTaskList(
    propertyRegistrationJourney: PropertyRegistrationJourney,
    journeyDataService: JourneyDataService,
    validator: Validator,
) : TaskList<RegisterPropertyStepId>(propertyRegistrationJourney, journeyDataService, validator) {
    override val taskList: List<Task<RegisterPropertyStepId>> =
        listOf(
            Task(
                "registerProperty.taskList.register.addAddress",
                RegisterPropertyStepId.LookupAddress,
                setOf(
                    RegisterPropertyStepId.LookupAddress,
                    RegisterPropertyStepId.SelectAddress,
                    RegisterPropertyStepId.ManualAddress,
                    RegisterPropertyStepId.AlreadyRegistered,
                    RegisterPropertyStepId.LocalAuthority,
                ),
            ),
            Task(
                "registerProperty.taskList.register.selectType",
                RegisterPropertyStepId.PropertyType,
            ),
            Task(
                "registerProperty.taskList.register.selectOwnership",
                RegisterPropertyStepId.OwnershipType,
            ),
            Task(
                "registerProperty.taskList.register.addLicensing",
                RegisterPropertyStepId.LicensingType,
                setOf(
                    RegisterPropertyStepId.LicensingType,
                    RegisterPropertyStepId.HmoMandatoryLicence,
                    RegisterPropertyStepId.HmoAdditionalLicence,
                    RegisterPropertyStepId.SelectiveLicence,
                ),
            ),
            Task(
                "registerProperty.taskList.register.addTenancyInfo",
                RegisterPropertyStepId.Occupancy,
                setOf(
                    RegisterPropertyStepId.Occupancy,
                    RegisterPropertyStepId.NumberOfHouseholds,
                    RegisterPropertyStepId.NumberOfPeople,
                ),
            ),
            Task(
                "registerProperty.taskList.register.selectOperation",
                RegisterPropertyStepId.LandlordType,
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
