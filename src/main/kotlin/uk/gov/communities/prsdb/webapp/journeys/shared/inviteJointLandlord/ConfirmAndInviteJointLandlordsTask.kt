package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.states.ConfirmAndInviteJointLandlordState

@JourneyFrameworkComponent
class ConfirmAndInviteJointLandlordsTask : Task<ConfirmAndInviteJointLandlordState>() {
    override fun makeSubJourney(state: ConfirmAndInviteJointLandlordState) =
        subJourney(state) {
            step(journey.hasJointLandlordsStep) {
                routeSegment(HasJointLandlordsStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.inviteJointLandlordsTask.firstStep
                        YesOrNo.NO -> exitStep
                    }
                }
            }
            task(journey.inviteJointLandlordsTask) {
                parents { journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES) }
                nextStep {
                    if (journey.invitedJointLandlords.isEmpty()) {
                        journey.hasJointLandlordsStep
                    } else {
                        exitStep
                    }
                }
                backStep { journey.hasJointLandlordsStep }
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
