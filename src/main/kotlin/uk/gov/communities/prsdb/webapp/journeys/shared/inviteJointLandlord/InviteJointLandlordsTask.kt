package uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InviteJointLandlordState

// A shared task for inviting joint landlords that owns its own four steps and IS its own InviteJointLandlordState, so it
// can be added to a journey more than once (each instance isolated by its route). The self-stated / route-scoping
// machinery lives in DuplicableTask; this class supplies the invite steps, the shared invited-emails state and the step
// flow. Journey-specific email context (existing invited/landlord emails, logged-in landlord email) is supplied by the
// concrete subclasses (PropertyRegistrationInviteJointLandlordsTask, UpdateInviteJointLandlordsTask), akin to the
// AddressTask content subclasses.
abstract class InviteJointLandlordsTask(
    journeyStateService: JourneyStateService,
    private val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep,
    override val inviteJointLandlordStep: InviteJointLandlordStep,
    override val inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    override val checkJointLandlordsStep: CheckJointLandlordsStep,
    override val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep,
) : DuplicableTask<InviteJointLandlordState>(journeyStateService),
    InviteJointLandlordState {
    override var invitedJointLandlordEmailsMap: Map<Int, String>? by delegateProvider.nullableDelegate("invitedJointLandlordEmails")
    override var nextJointLandlordMemberId: Int? by delegateProvider.nullableDelegate("nextJointLandlordMemberId")

    override val taskState get() = this

    override fun makeSubJourney(state: InviteJointLandlordState) =
        subJourney(state) {
            step(hasAnyJointLandlordsInvitedStep) {
                nextStep { mode ->
                    when (mode) {
                        AnyLandlordsInvited.NO_LANDLORDS -> journey.inviteJointLandlordStep
                        AnyLandlordsInvited.SOME_LANDLORDS -> journey.checkJointLandlordsStep
                    }
                }
            }
            step(journey.inviteJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_FIRST_ROUTE_SEGMENT)
                parents { hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.NO_LANDLORDS) }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.checkJointLandlordsStep) {
                routeSegment(CheckJointLandlordsStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.inviteJointLandlordStep.isComplete(),
                        hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS),
                    )
                }
                nextStep { exitStep }
            }
            step(journey.inviteAnotherJointLandlordStep) {
                routeSegment(InviteJointLandlordStep.INVITE_ANOTHER_ROUTE_SEGMENT)
                parents { hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS) }
                backStep { journey.checkJointLandlordsStep }
                nextStep { journey.checkJointLandlordsStep }
            }
            step(journey.removeJointLandlordAreYouSureStep) {
                routeSegment(RemoveJointLandlordAreYouSureStep.ROUTE_SEGMENT)
                parents {
                    hasAnyJointLandlordsInvitedStep.hasOutcome(AnyLandlordsInvited.SOME_LANDLORDS)
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
