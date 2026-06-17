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
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.InviteJointLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.RemoveJointLandlordAreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.shared.inviteJointLandlord.StartInviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.InviteJointLandlordState
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

        return if (propertyMarkedAsJointLandlord) {
            buildAlreadyJointLandlordInviteJourney(state, propertyDetailsLandlordTab, confirmationRoute)
        } else {
            buildNotCurrentlyJointLandlordInviteJourney(state, propertyDetailsLandlordTab, confirmationRoute)
        }
    }

    private fun buildAlreadyJointLandlordInviteJourney(
        state: InviteJointLandlordJourney,
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
                nextStep { journey.inviteJointLandlordsTask.firstStep }
            }
            task(journey.inviteJointLandlordsTask) {
                parents { journey.startInviteJointLandlordStep.isComplete() }
                backUrl { propertyDetailsLandlordTab }
                nextDestination { _ ->
                    if (journey.invitedJointLandlords.isEmpty()) {
                        Destination.ExternalUrl(propertyDetailsLandlordTab)
                    } else {
                        Destination(journey.checkInvitationsStep)
                    }
                }
            }
            step(journey.checkInvitationsStep) {
                routeSegment(CheckInvitationsStep.ROUTE_SEGMENT)
                parents { journey.inviteJointLandlordsTask.isComplete() }
                nextStep { journey.completeInviteJointLandlordStep }
            }
            step(journey.completeInviteJointLandlordStep) {
                parents { journey.checkInvitationsStep.isComplete() }
                nextUrl { confirmationRoute }
            }
        }

    private fun buildNotCurrentlyJointLandlordInviteJourney(
        state: InviteJointLandlordJourney,
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
                nextStep { journey.hasJointLandlordsStep }
            }
            step(journey.hasJointLandlordsStep) {
                routeSegment(HasJointLandlordsStep.ROUTE_SEGMENT)
                parents { journey.startInviteJointLandlordStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.inviteJointLandlordsTask.firstStep
                        YesOrNo.NO -> journey.completeInviteJointLandlordStep
                    }
                }
            }
            task(journey.inviteJointLandlordsTask) {
                parents { journey.hasJointLandlordsStep.hasOutcome(YesOrNo.YES) }
                backUrl { propertyDetailsLandlordTab }
                nextDestination { _ ->
                    if (journey.invitedJointLandlords.isEmpty()) {
                        Destination(journey.hasJointLandlordsStep)
                    } else {
                        Destination(journey.checkInvitationsStep)
                    }
                }
            }
            step(journey.checkInvitationsStep) {
                routeSegment(CheckInvitationsStep.ROUTE_SEGMENT)
                parents { journey.inviteJointLandlordsTask.isComplete() }
                nextStep { journey.completeInviteJointLandlordStep }
            }
            step(journey.completeInviteJointLandlordStep) {
                parents {
                    OrParents(
                        journey.checkInvitationsStep.isComplete(),
                        journey.hasJointLandlordsStep.hasOutcome(YesOrNo.NO),
                    )
                }
                nextUrl {
                    if (journey.hasJointLandlordsStep.outcome == YesOrNo.YES) {
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
    override val hasJointLandlordsStep: HasJointLandlordsStep,
    override val inviteJointLandlordStep: InviteJointLandlordStep,
    override val inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    override val checkJointLandlordsStep: CheckJointLandlordsStep,
    override val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep,
    override val inviteJointLandlordsTask: InviteJointLandlordsTask,
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
    InviteJointLandlordState {
    val startInviteJointLandlordStep: StartInviteJointLandlordStep
    val hasJointLandlordsStep: HasJointLandlordsStep
    val inviteJointLandlordsTask: InviteJointLandlordsTask
    val checkInvitationsStep: CheckInvitationsStep
    val completeInviteJointLandlordStep: CompleteInviteJointLandlordStep
    val propertyId: Long
}
