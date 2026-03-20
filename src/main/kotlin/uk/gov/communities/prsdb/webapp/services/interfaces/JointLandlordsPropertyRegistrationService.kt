package uk.gov.communities.prsdb.webapp.services.interfaces

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbFlip
import uk.gov.communities.prsdb.webapp.constants.JOINT_LANDLORDS
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel

interface JointLandlordsPropertyRegistrationService {
    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*>

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *>

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun addJointLandlordsJourneyTaskIfEnabled(addTask: () -> Unit)

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel>

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun addJointLandlordsCyaContent(
        state: PropertyRegistrationJourneyState,
        content: MutableMap<String, Any?>,
    )

    @PrsdbFlip(name = JOINT_LANDLORDS, alterBean = "joint-landlords-property-registration-flag-on")
    fun getJointLandlordEmailsForRegistration(state: PropertyRegistrationJourneyState): List<String>?
}
