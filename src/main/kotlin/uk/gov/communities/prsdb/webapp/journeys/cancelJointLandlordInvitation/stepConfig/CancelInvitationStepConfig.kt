package uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.stepConfig

import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.cancelJointLandlordInvitation.CancelJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@JourneyFrameworkComponent
class CancelInvitationStepConfig(
    private val jointLandlordInvitationService: JointLandlordInvitationService,
) : AbstractInternalStepConfig<Complete, CancelJointLandlordInvitationJourneyState>() {
    override fun mode(state: CancelJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: CancelJointLandlordInvitationJourneyState) {
        val baseUserId = SecurityContextHolder.getContext().authentication.name
        val invitation = jointLandlordInvitationService.getPendingInvitationIfAuthorizedLandlord(state.invitationId, baseUserId)

        // Cancel the invitation
        jointLandlordInvitationService.cancelInvitation(invitation)

        // Store cancelled email in session for confirmation page
        jointLandlordInvitationService.addOrUpdateCancelledInvitationEmailInSession(state.invitedEmail)
        // TODO: See next PR
    }

    override fun resolveNextDestination(
        state: CancelJointLandlordInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class CancelInvitationStep(
    stepConfig: CancelInvitationStepConfig,
) : JourneyStep.InternalStep<Complete, CancelJointLandlordInvitationJourneyState>(stepConfig)
