package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.InviteJointLandlordPropertyRegistrationState

@JourneyFrameworkComponent
class JointLandlordsPropertyRegistrationTask : Task<InviteJointLandlordPropertyRegistrationState>() {
    override fun makeSubJourney(state: InviteJointLandlordPropertyRegistrationState) =
        subJourney(state) {
            taskStatus {
                when {
                    exitStep.isStepReachable -> TaskStatus.COMPLETED
                    journey.hasJointLandlordsStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.hasAnyJointLandlordsInvitedStep.outcome == AnyLandlordsInvited.SOME_LANDLORDS -> TaskStatus.IN_PROGRESS
                    firstStep.isStepReachable -> TaskStatus.NOT_STARTED
                    else -> TaskStatus.CANNOT_START
                }
            }
            // this is split out into its own task to be common with the 'Add a JL' button on property details
            task(journey.inviteJointLandlordsTask) {
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.inviteJointLandlordsTask.isComplete() }
            }
        }
}
