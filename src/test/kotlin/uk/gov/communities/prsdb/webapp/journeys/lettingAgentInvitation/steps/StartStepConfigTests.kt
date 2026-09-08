package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class StartStepConfigTests {
    @Mock
    lateinit var mockLettingAgentAccessService: LettingAgentAccessService

    @Mock
    lateinit var mockState: LettingAgentInvitationJourneyState

    @Test
    fun `afterStepIsReached populates the invitation token from the session for this journey`() {
        val journeyId = "journey-123"
        val token = UUID.randomUUID().toString()
        whenever(mockState.journeyId).thenReturn(journeyId)
        whenever(mockLettingAgentAccessService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)

        StartStepConfig(mockLettingAgentAccessService).afterStepIsReached(mockState)

        verify(mockState).invitationToken = token
    }
}
