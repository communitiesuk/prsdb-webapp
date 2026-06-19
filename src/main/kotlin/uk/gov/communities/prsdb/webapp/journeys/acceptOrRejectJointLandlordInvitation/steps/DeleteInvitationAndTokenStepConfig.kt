package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@JourneyFrameworkComponent
class DeleteInvitationAndTokenStepConfig(
    private val invitationService: JointLandlordInvitationService,
) : AbstractInternalStepConfig<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>() {
    override fun mode(state: AcceptOrRejectJointLandlordInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: AcceptOrRejectJointLandlordInvitationJourneyState) {
        val invitation = invitationService.getInvitationForJourney(state.journeyId)
        invitationService.removeInvitation(invitation)

        val token = invitationService.getInvitationTokenForJourneyIdFromSession(state.journeyId)
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession(token)
    }

    override fun resolveNextDestination(
        state: AcceptOrRejectJointLandlordInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        state.deleteJourney()
        return defaultDestination
    }
}

@JourneyFrameworkComponent
class DeleteInvitationAndTokenStep(
    stepConfig: DeleteInvitationAndTokenStepConfig,
) : JourneyStep.InternalStep<Complete, AcceptOrRejectJointLandlordInvitationJourneyState>(stepConfig)
