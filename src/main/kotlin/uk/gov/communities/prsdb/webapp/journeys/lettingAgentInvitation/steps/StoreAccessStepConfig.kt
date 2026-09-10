package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController
import uk.gov.communities.prsdb.webapp.journeys.AbstractInternalStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
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
        val token = state.invitationToken
        val invitation = lettingAgentAccessService.getInvitationByTokenOrNull(UUID.fromString(token)) ?: return
        if (lettingAgentPasswordService.hasPasswordBeenSet(invitation)) {
            lettingAgentAccessService.addAuthorisedTokenToSession(token)
        }
    }

    override fun resolveNextDestination(
        state: LettingAgentInvitationJourneyState,
        defaultDestination: Destination,
    ): Destination {
        val token = state.invitationToken
        return if (!lettingAgentAccessService.getTokenIsValid(token)) {
            Destination.ExternalUrl(LettingAgentInvitationController.LETTING_AGENT_INVALID_LINK_ROUTE)
        } else {
            defaultDestination
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
