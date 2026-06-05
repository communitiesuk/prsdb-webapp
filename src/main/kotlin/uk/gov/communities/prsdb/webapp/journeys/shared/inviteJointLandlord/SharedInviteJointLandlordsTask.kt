package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.shared.states.SharedInviteJointLandlordState

@JourneyFrameworkComponent
class SharedInviteJointLandlordsTask : Task<SharedInviteJointLandlordState>() {
    override fun makeSubJourney(state: SharedInviteJointLandlordState) =
        subJourney(state) {
            step(journey.inviteJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT)
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.checkJointLandlordsStep) {
                routeSegment(CheckJointLandlordsStep.ROUTE_SEGMENT)
                parents { journey.inviteJointLandlordStep.isComplete() }
                nextStep { exitStep }
            }
            step(journey.inviteAnotherJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT)
                parents { journey.inviteJointLandlordStep.isComplete() }
                backStep { journey.checkJointLandlordsStep }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.removeJointLandlordAreYouSureStep) {
                routeSegment(RemoveJointLandlordAreYouSureStep.ROUTE_SEGMENT)
                parents { journey.inviteJointLandlordStep.isComplete() }
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
