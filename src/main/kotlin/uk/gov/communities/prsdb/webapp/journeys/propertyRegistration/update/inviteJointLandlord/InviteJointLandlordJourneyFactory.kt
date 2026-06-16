package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.ConfirmAndInviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.RemoveJointLandlordAreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.StartInviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.ConfirmAndInviteJointLandlordState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import java.security.Principal

@PrsdbWebService
class InviteJointLandlordJourneyFactory(
    private val stateFactory: ObjectFactory<InviteJointLandlordJourney>,
    private val propertyOwnershipService: PropertyOwnershipService,
) {
    final fun createJourneySteps(propertyId: Long): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        if (!state.isStateInitialized) {
            state.propertyId = propertyId
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val propertyMarkedAsJointLandlord = propertyOwnershipService.getPropertyOwnership(propertyId).markedJointLandlord
        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)
        val propertyDetailsLandlordTab = "$propertyDetailsRoute#$LANDLORD_DETAILS_FRAGMENT"
        val confirmationRoute =
            InviteJointLandlordController.getInviteJointLandlordRoute(propertyId) + "/$CONFIRMATION_PATH_SEGMENT"

        return buildJourney(state, propertyMarkedAsJointLandlord, propertyDetailsLandlordTab, confirmationRoute)
    }

    private fun buildJourney(
        state: InviteJointLandlordJourney,
        propertyMarkedAsJointLandlord: Boolean,
        propertyDetailsLandlordTab: String,
        confirmationRoute: String,
    ): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            unreachableStepUrl { propertyDetailsLandlordTab }
            configure {
                withAdditionalContentProperty { "title" to "inviteJointLandlord.title" }
            }
            // TODO: PDJB-896 - Replace startInviteJointLandlordStep with addressable tasks when implemented
            step(journey.startInviteJointLandlordStep) {
                routeSegment(StartInviteJointLandlordStep.ROUTE_SEGMENT)
                initialStep()
                nextStep {
                    if (propertyMarkedAsJointLandlord) {
                        journey.inviteJointLandlordsTask.firstStep
                    } else {
                        journey.confirmAndInviteJointLandlordsTask.firstStep
                    }
                }
            }
            if (propertyMarkedAsJointLandlord) {
                task(journey.inviteJointLandlordsTask) {
                    parents { journey.startInviteJointLandlordStep.isComplete() }
                    backUrl { propertyDetailsLandlordTab }
                    nextStep { journey.checkInvitationsStep }
                }
            } else {
                task(journey.confirmAndInviteJointLandlordsTask) {
                    parents { journey.startInviteJointLandlordStep.isComplete() }
                    backUrl { propertyDetailsLandlordTab }
                    nextDestination { _ ->
                        if (journey.hasJointLandlordsStep.outcome == YesOrNo.YES) {
                            Destination(journey.checkInvitationsStep)
                        } else {
                            Destination(journey.completeInviteJointLandlordStep)
                        }
                    }
                }
            }
            step(journey.checkInvitationsStep) {
                routeSegment(CheckInvitationsStep.ROUTE_SEGMENT)
                parents {
                    if (propertyMarkedAsJointLandlord) {
                        journey.inviteJointLandlordsTask.isComplete()
                    } else {
                        journey.confirmAndInviteJointLandlordsTask.isComplete()
                    }
                }
                nextStep { journey.completeInviteJointLandlordStep }
            }
            step(journey.completeInviteJointLandlordStep) {
                parents {
                    if (propertyMarkedAsJointLandlord) {
                        journey.checkInvitationsStep.isComplete()
                    } else {
                        OrParents(
                            journey.checkInvitationsStep.isComplete(),
                            journey.confirmAndInviteJointLandlordsTask.isComplete(),
                        )
                    }
                }
                nextUrl {
                    if (propertyMarkedAsJointLandlord || journey.hasJointLandlordsStep.outcome == YesOrNo.YES) {
                        confirmationRoute
                    } else {
                        propertyDetailsLandlordTab
                    }
                }
            }
        }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class InviteJointLandlordJourney(
    override val startInviteJointLandlordStep: StartInviteJointLandlordStep,
    override val hasAnyJointLandlordsInvitedStep: HasAnyJointLandlordsInvitedStep,
    override val hasJointLandlordsStep: HasJointLandlordsStep,
    override val inviteJointLandlordStep: InviteJointLandlordStep,
    override val inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    override val checkJointLandlordsStep: CheckJointLandlordsStep,
    override val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep,
    override val inviteJointLandlordsTask: InviteJointLandlordsTask,
    override val confirmAndInviteJointLandlordsTask: ConfirmAndInviteJointLandlordsTask,
    override val checkInvitationsStep: CheckInvitationsStep,
    override val completeInviteJointLandlordStep: CompleteInviteJointLandlordStep,
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val propertyOwnershipService: PropertyOwnershipService,
    journeyStateService: JourneyStateService,
    journeyName: String = "inviteJointLandlord",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    InviteJointLandlordJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var invitedJointLandlordEmailsMap: Map<Int, String>? by delegateProvider.nullableDelegate("invitedJointLandlordEmails")
    override var nextJointLandlordMemberId: Int? by delegateProvider.nullableDelegate("nextJointLandlordMemberId")

    override val existingInvitedEmails: List<String>
        get() = jointLandlordInvitationService.getExistingInvitedEmails(propertyId)

    override val existingLandlordEmails: List<String>
        get() = propertyOwnershipService.getPropertyOwnership(propertyId).landlords.map { it.email }
}

interface InviteJointLandlordJourneyState :
    JourneyState,
    ConfirmAndInviteJointLandlordState {
    val startInviteJointLandlordStep: StartInviteJointLandlordStep
    val checkInvitationsStep: CheckInvitationsStep
    val completeInviteJointLandlordStep: CompleteInviteJointLandlordStep
    val propertyId: Long
}
