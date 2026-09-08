package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.InternalStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.LettingAgentPasswordService
import java.util.UUID

@JourneyFrameworkComponent
class StoreAccessStepConfig(
    private val lettingAgentAccessService: LettingAgentAccessService,
    private val lettingAgentPasswordService: LettingAgentPasswordService,
) : AbstractInternalStepConfig<Complete, LettingAgentInvitationJourneyState>() {
    override fun mode(state: LettingAgentInvitationJourneyState): Complete = Complete.COMPLETE

    override fun afterStepIsReached(state: LettingAgentInvitationJourneyState) {
        val token = requireNotNull(state.invitationToken) { "Invitation token is missing from the journey state" }
        val invitation = lettingAgentAccessService.getInvitationByToken(UUID.fromString(token))
        // Defensive: only grant session access if the invitation provably has a password set/entered,
        // rather than trusting the journey's hasSetNewPassword/hasEnteredPassword flags.
        if (lettingAgentPasswordService.hasPasswordBeenSet(invitation)) {
            lettingAgentAccessService.addAuthorisedTokenToSession(token)
        }
    }
}

/**
 * This step silently stores the validated invitation token in the session so the letting-agent access
 * interceptor will allow this session to view the property's letting-agent pages.
 */
@JourneyFrameworkComponent
final class StoreAccessStep(
    stepConfig: StoreAccessStepConfig,
) : InternalStep<Complete, LettingAgentInvitationJourneyState>(stepConfig)
