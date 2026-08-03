package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService

@JourneyFrameworkComponent
class CompleteInviteJointLandlordStepConfig(
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val propertyOwnershipService: PropertyOwnershipService,
    private val userToLandlordService: UserToLandlordService,
) : AbstractInternalStepConfig<Complete, InviteJointLandlordJourneyState>() {
    override fun mode(state: InviteJointLandlordJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: InviteJointLandlordJourneyState) {
        if (state.inviteJointLandlordsTask.invitedJointLandlords.isNotEmpty()) {
            val loggedInLandlord = userToLandlordService.getCurrentLandlordForUser()

            val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId)

            propertyOwnershipService.markAsJointLandlord(propertyOwnership)
            jointLandlordInvitationService.sendInvitationEmails(
                jointLandlordEmails = state.inviteJointLandlordsTask.invitedJointLandlords,
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
