package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel

interface JointLandlordsPropertyRegistrationStrategy {
    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*>

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *>

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun ifEnabled(action: () -> Unit)

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel>

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getJointLandlordEmailsForRegistration(state: PropertyRegistrationJourneyState): List<String>?
}

@Primary
@PrsdbWebService("joint-landlords-property-registration-flag-off")
class JointLandlordsPropertyRegistrationStrategyImplFlagOff : JointLandlordsPropertyRegistrationStrategy {
    override fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*> = state.occupationTask

    override fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *> = state.gasSafetyTask.firstStep

    override fun ifEnabled(action: () -> Unit) {}

    override fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel> = emptyList()

    override fun getJointLandlordEmailsForRegistration(state: PropertyRegistrationJourneyState): List<String>? = null
}

@PrsdbWebService("joint-landlords-property-registration-flag-on")
class JointLandlordsPropertyRegistrationStrategyImplFlagOn : JointLandlordsPropertyRegistrationStrategy {
    override fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*> = state.jointLandlordsTask

    override fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *> = state.jointLandlordsTask.firstStep

    override fun ifEnabled(action: () -> Unit) {
        action()
    }

    override fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel> =
        listOf(
            TaskListItemViewModel.fromTask(
                "registerProperty.taskList.register.inviteJointLandlords",
                state.jointLandlordsTask,
                "registerProperty.taskList.register.inviteJointLandlords.hint",
            ),
        )

    override fun getJointLandlordEmailsForRegistration(state: PropertyRegistrationJourneyState): List<String>? =
        state.invitedJointLandlordEmailsMap?.values?.toList()
}
