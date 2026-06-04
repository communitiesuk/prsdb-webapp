package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent
class JointLandlordsTask : Task<JointLandlordsState>() {
    override fun makeSubJourney(state: JointLandlordsState) =
        subJourney(state) {
            taskStatus {
                when {
                    exitStep.isStepReachable -> TaskStatus.COMPLETED
                    journey.hasJointLandlordsStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.checkJointLandlordsStep.outcome != null -> TaskStatus.IN_PROGRESS
                    journey.hasAnyJointLandlordsInvitedStep.outcome == AnyLandlordsInvited.SOME_LANDLORDS -> TaskStatus.IN_PROGRESS
                    firstStep.isStepReachable -> TaskStatus.NOT_STARTED
                    else -> TaskStatus.CANNOT_START
                }
            }
            step(journey.hasAnyJointLandlordsInvitedStep) {
                nextStep { mode ->
                    when (mode) {
                        AnyLandlordsInvited.NO_LANDLORDS -> journey.hasJointLandlordsStep
                        AnyLandlordsInvited.SOME_LANDLORDS -> journey.checkJointLandlordsStep
                    }
                }
            }
            step(journey.hasJointLandlordsStep) {
                routeSegment(HasJointLandlordsStep.ROUTE_SEGMENT)
                parents { journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.NO_LANDLORDS) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.inviteJointLandlordsTask.firstStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            task(journey.inviteJointLandlordsTask) {
                parents {
                    OrParents(
                        journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES),
                        journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS),
                    )
                }
                nextDestination { _ ->
                    if (journey.invitedJointLandlords.isEmpty()) {
                        Destination(journey.hasJointLandlordsStep)
                    } else {
                        Destination(exitStep)
                    }
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.inviteJointLandlordsTask.isComplete(),
                        journey.hasJointLandlordsStep.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
