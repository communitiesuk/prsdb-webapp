package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel

@PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
interface JointLandlordsPropertyRegistrationStrategy {
    fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*>

    fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *>

    fun ifEnabled(action: () -> Unit)

    fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel>
}

@Primary
@PrsdbWebService("joint-landlords-property-registration-flag-off")
class JointLandlordsPropertyRegistrationStrategyImplFlagOff : JointLandlordsPropertyRegistrationStrategy {
    override fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*> = state.occupationTask

    override fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *> = state.gasSafetyTask.firstStep

    override fun ifEnabled(action: () -> Unit) {}

    override fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel> = emptyList()
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
}
