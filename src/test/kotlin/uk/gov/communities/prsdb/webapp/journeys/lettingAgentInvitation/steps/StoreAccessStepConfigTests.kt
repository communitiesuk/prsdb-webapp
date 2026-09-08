package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import uk.gov.communities.prsdb.webapp.services.LettingAgentPasswordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class StoreAccessStepConfigTests {
    @Mock
    lateinit var mockLettingAgentAccessService: LettingAgentAccessService

    @Mock
    lateinit var mockLettingAgentPasswordService: LettingAgentPasswordService

    @Mock
    lateinit var mockState: LettingAgentInvitationJourneyState

    @Test
    fun `afterStepIsReached grants the token to the session when the invitation has a password`() {
        val token = UUID.randomUUID()
        val invitation = MockLettingAgentData.createLettingAgentAccess(token = token)
        whenever(mockState.invitationToken).thenReturn(token.toString())
        whenever(mockLettingAgentAccessService.getInvitationByToken(token)).thenReturn(invitation)
        whenever(mockLettingAgentPasswordService.hasPasswordBeenSet(invitation)).thenReturn(true)

        setupStepConfig().afterStepIsReached(mockState)

        verify(mockLettingAgentAccessService).addAuthorisedTokenToSession(token.toString())
    }

    @Test
    fun `afterStepIsReached does not grant the token when the invitation has no password`() {
        val token = UUID.randomUUID()
        val invitation = MockLettingAgentData.createLettingAgentAccessWithoutPassword(token = token)
        whenever(mockState.invitationToken).thenReturn(token.toString())
        whenever(mockLettingAgentAccessService.getInvitationByToken(token)).thenReturn(invitation)
        whenever(mockLettingAgentPasswordService.hasPasswordBeenSet(invitation)).thenReturn(false)

        setupStepConfig().afterStepIsReached(mockState)

        verify(mockLettingAgentAccessService, never()).addAuthorisedTokenToSession(token.toString())
    }

    private fun setupStepConfig() = StoreAccessStepConfig(mockLettingAgentAccessService, mockLettingAgentPasswordService)
}
