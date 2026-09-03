package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

// TODO: PDJB-1659: Remove this in favour of an interceptor
@JourneyFrameworkComponent("lettingAgentInvitationValidateTokenStepConfig")
class ValidateTokenStepConfig(
    private val lettingAgentAccessService: LettingAgentAccessService,
) : AbstractInternalStepConfig<TokenValidationResult, LettingAgentInvitationJourneyState>() {
    override fun mode(state: LettingAgentInvitationJourneyState): TokenValidationResult? {
        val token = state.invitationToken ?: return null
        return if (lettingAgentAccessService.getTokenIsValid(token)) {
            TokenValidationResult.VALID
        } else {
            TokenValidationResult.INVALID
        }
    }

    override fun afterStepIsReached(state: LettingAgentInvitationJourneyState) {
        val token = lettingAgentAccessService.getInvitationTokenForJourneyIdFromSession(state.journeyId)
        state.invitationToken = token
    }
}

@JourneyFrameworkComponent("lettingAgentInvitationValidateTokenStep")
final class ValidateTokenStep(
    stepConfig: ValidateTokenStepConfig,
) : JourneyStep.InternalStep<TokenValidationResult, LettingAgentInvitationJourneyState>(stepConfig)

enum class TokenValidationResult {
    VALID,
    INVALID,
}
