package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.TaskStatus
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordAreYouSureStep
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
                        YesOrNo.YES -> journey.inviteJointLandlordStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            step(journey.inviteJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT)
                parents { journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.checkJointLandlordsStep) {
                routeSegment(CheckJointLandlordsStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.inviteJointLandlordStep.isComplete(),
                        journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS),
                    )
                }
                nextStep { exitStep }
            }
            step(journey.inviteAnotherJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT)
                parents { journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS) }
                // If no back step is set, the parent is used instead.
                // If an internal step is the back step, the journey will go back to the nearest visitable ancestor of the internal step.
                // In this case, we want to go back to the check step, so we need to explicitly set the back step.
                backStep { journey.checkJointLandlordsStep }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.removeJointLandlordAreYouSureStep) {
                routeSegment(RemoveJointLandlordAreYouSureStep.ROUTE_SEGMENT)
                parents {
                    journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS)
                }
                backStep { journey.checkJointLandlordsStep }
                nextStep { mode ->
                    when (mode) {
                        AnyLandlordsInvited.SOME_LANDLORDS -> journey.checkJointLandlordsStep
                        AnyLandlordsInvited.NO_LANDLORDS -> journey.hasJointLandlordsStep
                    }
                }
                exitStep {
                    parents {
                        OrParents(
                            journey.checkJointLandlordsStep.isComplete(),
                            journey.hasJointLandlordsStep.hasOutcome(YesOrNo.NO),
                        )
                    }
                }
            }
        }
}
