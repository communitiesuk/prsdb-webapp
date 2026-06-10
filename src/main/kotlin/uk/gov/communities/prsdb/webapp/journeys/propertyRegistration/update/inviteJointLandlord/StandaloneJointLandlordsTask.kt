package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.RemoveJointLandlordAreYouSureStep

@JourneyFrameworkComponent
class StandaloneJointLandlordsTask : Task<InviteJointLandlordJourneyState>() {
    override fun makeSubJourney(state: InviteJointLandlordJourneyState) =
        subJourney(state) {
            step(journey.startInviteJointLandlordStep) {
                routeSegment(StartInviteJointLandlordStep.ROUTE_SEGMENT)
                nextStep { journey.hasAnyJointLandlordsInvitedStep }
            }
            step(journey.hasAnyJointLandlordsInvitedStep) {
                parents { journey.startInviteJointLandlordStep.isComplete() }
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
                backUrl { state.checkJointLandlordsBackUrl }
                nextStep { journey.checkInvitationsStep }
            }
            step(journey.inviteAnotherJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.inviteJointLandlordStep.isComplete(),
                        journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS),
                    )
                }
                backStep { journey.checkJointLandlordsStep }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.removeJointLandlordAreYouSureStep) {
                routeSegment(RemoveJointLandlordAreYouSureStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.inviteJointLandlordStep.isComplete(),
                        journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS),
                    )
                }
                backStep { journey.checkJointLandlordsStep }
                nextStep { mode ->
                    when (mode) {
                        AnyLandlordsInvited.SOME_LANDLORDS -> journey.checkJointLandlordsStep
                        AnyLandlordsInvited.NO_LANDLORDS -> exitStep
                    }
                }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.checkJointLandlordsStep.isComplete(),
                        journey.removeJointLandlordAreYouSureStep.hasOutcome(AnyLandlordsInvited.NO_LANDLORDS),
                    )
                }
            }
        }
}
