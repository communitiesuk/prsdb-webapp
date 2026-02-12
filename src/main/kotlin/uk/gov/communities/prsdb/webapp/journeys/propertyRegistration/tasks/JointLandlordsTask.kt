package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// TODO PDJB-117: Implement joint landlord task logic
@JourneyFrameworkComponent
class JointLandlordsTask : Task<JointLandlordsState>() {
    override fun makeSubJourney(state: JointLandlordsState) =
        subJourney(state) {
            step(journey.hasJointLandlordsInternalStep) {
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.NO -> journey.hasJointLandlordsStep
                        YesOrNo.YES -> journey.checkJointLandlordsStep
                    }
                }
            }
            step(journey.hasJointLandlordsStep) {
                routeSegment(RegisterPropertyStepId.HasJointLandlords.urlPathSegment)
                parents { journey.hasJointLandlordsInternalStep.hasOutcome(YesOrNo.NO) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.inviteJointLandlordStep
                        YesOrNo.NO -> exitStep
                    }
                }
                savable()
            }
            step(journey.inviteJointLandlordStep) {
                routeSegment(RegisterPropertyStepId.InviteJointLandlord.urlPathSegment)
                parents { journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.checkJointLandlordsStep) {
                routeSegment(RegisterPropertyStepId.CheckJointLandlords.urlPathSegment)
                parents {
                    OrParents(
                        journey.inviteJointLandlordStep.isComplete(),
                        journey.hasJointLandlordsInternalStep.hasOutcome(YesOrNo.YES),
                    )
                }
                nextStep { exitStep }
            }
            step(journey.inviteAnotherJointLandlordStep) {
                routeSegment("invite-another-joint-landlord")
                parents { journey.hasJointLandlordsInternalStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.removeJointLandlordStep) {
                routeSegment(RegisterPropertyStepId.RemoveJointLandlord.urlPathSegment)
                parents { journey.checkJointLandlordsStep.isComplete() }
                nextStep { journey.checkJointLandlordsStep }
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
