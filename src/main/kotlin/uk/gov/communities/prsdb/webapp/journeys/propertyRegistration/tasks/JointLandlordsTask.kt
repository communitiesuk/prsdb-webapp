package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.JointLandlordsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

// TODO PDJB-117: Implement joint landlord task logic
@JourneyFrameworkComponent
class JointLandlordsTask : Task<JointLandlordsState>() {
    override fun makeSubJourney(state: JointLandlordsState) =
        subJourney(state) {
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
                routeSegment(InviteJointLandlordStep.ROUTE_SEGMENT)
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
                routeSegment(InviteJointLandlordStep.ROUTE_SEGMENT)
                parents { journey.hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS) }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.removeJointLandlordStep) {
                routeSegment(RemoveJointLandlordStep.ROUTE_SEGMENT)
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
