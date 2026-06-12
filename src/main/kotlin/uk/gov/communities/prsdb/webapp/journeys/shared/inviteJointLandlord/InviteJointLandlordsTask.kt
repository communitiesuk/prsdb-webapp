package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InviteJointLandlordState

@JourneyFrameworkComponent
class InviteJointLandlordsTask : Task<InviteJointLandlordState>() {
    override fun makeSubJourney(state: InviteJointLandlordState) =
        subJourney(state) {
            step(journey.isMarkedAsJointLandlordStep) {
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.hasAnyJointLandlordsInvitedStep
                        YesOrNo.NO -> journey.hasJointLandlordsStep
                    }
                }
            }
            step(journey.hasJointLandlordsStep) {
                routeSegment(HasJointLandlordsStep.ROUTE_SEGMENT)
                parents { journey.isMarkedAsJointLandlordStep.hasOutcome(YesOrNo.NO) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.hasAnyJointLandlordsInvitedStep
                        YesOrNo.NO -> exitStep
                    }
                }
            }
            step(journey.hasAnyJointLandlordsInvitedStep) {
                parents {
                    OrParents(
                        journey.isMarkedAsJointLandlordStep.hasOutcome(YesOrNo.YES),
                        journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        AnyLandlordsInvited.NO_LANDLORDS -> journey.inviteJointLandlordStep
                        AnyLandlordsInvited.SOME_LANDLORDS -> journey.checkJointLandlordsStep
                    }
                }
            }
            step(journey.inviteJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT)
                parents { journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.NO_LANDLORDS) }
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
