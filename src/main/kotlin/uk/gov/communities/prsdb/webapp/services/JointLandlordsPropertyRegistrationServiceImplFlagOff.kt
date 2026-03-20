package uk.gov.communities.prsdb.webapp.services

import org.springframework.context.annotation.Primary
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.PropertyRegistrationJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.taskModels.TaskListItemViewModel
import uk.gov.communities.prsdb.webapp.services.interfaces.JointLandlordsPropertyRegistrationService

@Primary
@PrsdbWebService("joint-landlords-property-registration-flag-off")
class JointLandlordsPropertyRegistrationServiceImplFlagOff : JointLandlordsPropertyRegistrationService {
    override fun getLastPreComplianceTask(state: PropertyRegistrationJourneyState): Task<*> = state.occupationTask

    override fun getOccupationNextStep(state: PropertyRegistrationJourneyState): JourneyStep<*, *, *> = state.gasSafetyTask.firstStep

    override fun addJointLandlordsJourneyTaskIfEnabled(addTask: () -> Unit) {
        // No-op: joint landlords task is not included in the journey when the feature is disabled
    }

    override fun getJointLandlordsTaskListItems(state: PropertyRegistrationJourneyState): List<TaskListItemViewModel> = emptyList()

    override fun addJointLandlordsCyaContent(
        state: PropertyRegistrationJourneyState,
        content: MutableMap<String, Any?>,
    ) {
        // No-op: joint landlords section is not shown on CYA when the feature is disabled
    }

    override fun getJointLandlordEmailsForRegistration(state: PropertyRegistrationJourneyState): List<String>? = null
}
