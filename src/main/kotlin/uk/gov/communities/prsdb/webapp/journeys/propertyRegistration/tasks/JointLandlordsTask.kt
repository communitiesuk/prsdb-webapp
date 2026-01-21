package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.example.steps.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState

// TODO PDJB-112, PDJB-113, PDJB-114, PDJB-117: Implement joint landlord task logic
@JourneyFrameworkComponent
class JointLandlordsTask : Task<JointLandlordsState>() {
    override fun makeSubJourney(state: JointLandlordsState) =
        subJourney(state) {
            step(journey.hasJointLandlordsStep) {
                routeSegment(RegisterPropertyStepId.HasJointLandlords.urlPathSegment)
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.addJointLandlordStep
                        YesOrNo.NO -> exitStep
                    }
                }
            }
            step(journey.addJointLandlordStep) {
                routeSegment(RegisterPropertyStepId.AddJointLandlord.urlPathSegment)
                parents { journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.checkJointLandlordsStep) {
                routeSegment(RegisterPropertyStepId.CheckJointLandlords.urlPathSegment)
                parents { journey.addJointLandlordStep.isComplete() }
                nextStep { journey.removeJointLandlordStep }
            }
            step(journey.removeJointLandlordStep) {
                routeSegment(RegisterPropertyStepId.RemoveJointLandlord.urlPathSegment)
                parents { journey.checkJointLandlordsStep.isComplete() }
                nextStep { exitStep }
            }
            exitStep {
                parents {
                    OrParents(
                        journey.removeJointLandlordStep.isComplete(),
                        journey.hasJointLandlordsStep.hasOutcome(YesOrNo.NO),
                    )
                }
            }
        }
}
