package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.inviteJointLandlord

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class CompleteInviteJointLandlordStepConfig(
    private val jointLandlordInvitationService: JointLandlordInvitationService,
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractInternalStepConfig<Complete, InviteJointLandlordJourneyState>() {
    override fun mode(state: InviteJointLandlordJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: InviteJointLandlordJourneyState) {
        val propertyOwnership = propertyOwnershipService.getPropertyOwnership(state.propertyId)
        // TODO PDJB-1069 - do not use primary landlord when it is not needed
        jointLandlordInvitationService.sendInvitationEmails(
            jointLandlordEmails = state.invitedJointLandlords,
            propertyOwnership = propertyOwnership,
            invitingLandlord = propertyOwnership.primaryLandlord,
        )
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
