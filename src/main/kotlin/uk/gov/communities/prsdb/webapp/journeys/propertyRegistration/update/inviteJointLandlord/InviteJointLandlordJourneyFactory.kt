package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_DETAILS_FRAGMENT
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractPropertyOwnershipUpdateJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SharedInviteJointLandlordState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.InviteJointLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemoveJointLandlordAreYouSureStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.SharedInviteJointLandlordsTask
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
            state.lastModifiedDate = propertyOwnershipService.getPropertyOwnership(propertyId).getMostRecentlyUpdated().toString()
            state.isStateInitialized = true
        }

        if (state.propertyId != propertyId) {
            throw PrsdbWebException("Journey state propertyId ${state.propertyId} does not match provided propertyId $propertyId")
        }

        val propertyDetailsRoute = PropertyDetailsController.getPropertyDetailsPath(propertyId)
        val propertyDetailsLandlordTab = "$propertyDetailsRoute#$LANDLORD_DETAILS_FRAGMENT"
        val confirmationRoute =
            InviteJointLandlordController.getInviteJointLandlordRoute(propertyId) + "/confirmation"

        return journey(state) {
            unreachableStepUrl { propertyDetailsLandlordTab }
            configure {
                withAdditionalContentProperty { "title" to "inviteJointLandlord.title" }
            }
            task(journey.inviteJointLandlordsTask) {
                initialStep()
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
    }

    fun initializeJourneyState(
        ownershipId: Long,
        user: Principal,
    ): String = stateFactory.getObject().initializeOrRestoreState(Pair(ownershipId, user))
}

@JourneyFrameworkComponent
class InviteJointLandlordJourney(
    override val inviteJointLandlordStep: InviteJointLandlordStep,
    override val inviteAnotherJointLandlordStep: InviteJointLandlordStep,
    override val checkJointLandlordsStep: CheckJointLandlordsStep,
    override val removeJointLandlordAreYouSureStep: RemoveJointLandlordAreYouSureStep,
    override val inviteJointLandlordsTask: SharedInviteJointLandlordsTask,
    override val checkInvitationsStep: CheckInvitationsStep,
    override val completeInviteJointLandlordStep: CompleteInviteJointLandlordStep,
    journeyStateService: JourneyStateService,
    journeyName: String = "inviteJointLandlord",
) : AbstractPropertyOwnershipUpdateJourneyState(journeyStateService, journeyName),
    InviteJointLandlordJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var propertyId: Long by delegateProvider.requiredImmutableDelegate("propertyId")
    override var lastModifiedDate: String by delegateProvider.requiredImmutableDelegate("lastModifiedDate")
    override var invitedJointLandlordEmailsMap: Map<Int, String>? by delegateProvider.nullableDelegate("invitedJointLandlordEmails")
    override var nextJointLandlordMemberId: Int? by delegateProvider.nullableDelegate("nextJointLandlordMemberId")
}

interface InviteJointLandlordJourneyState :
    JourneyState,
    SharedInviteJointLandlordState {
    val inviteJointLandlordsTask: SharedInviteJointLandlordsTask
    val checkInvitationsStep: CheckInvitationsStep
    val completeInviteJointLandlordStep: CompleteInviteJointLandlordStep
    val propertyId: Long
    val lastModifiedDate: String
}
