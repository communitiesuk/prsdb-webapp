package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteInviteJointLandlordStepConfig(
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val landlordService: LandlordService,
) : AbstractInternalStepConfig<Complete, InviteJointLandlordJourneyState>() {
    override fun mode(state: InviteJointLandlordJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: InviteJointLandlordJourneyState) {
        if (state.invitedJointLandlords.isNotEmpty()) {
            val baseUserId = SecurityContextHolder.getContext().authentication.name
            val loggedInLandlord =
                landlordService.retrieveLandlordByBaseUserId(baseUserId)
                    ?: throw PrsdbWebException(
                        "Landlord record not found for logged in user with baseUserId $baseUserId",
                    )

            val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId)

            propertyOwnershipService.markAsJointLandlord(propertyOwnership)
            jointLandlordInvitationService.sendInvitationEmails(
                jointLandlordEmails = state.invitedJointLandlords,
                propertyOwnership = propertyOwnership,
                invitingLandlord = loggedInLandlord,
            )
        }
    }

    override fun resolveNextDestination(
        state: InviteJointLandlordJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CompleteInviteJointLandlordStep(
    stepConfig: CompleteInviteJointLandlordStepConfig,
) : JourneyStep.InternalStep<Complete, InviteJointLandlordJourneyState>(stepConfig)
