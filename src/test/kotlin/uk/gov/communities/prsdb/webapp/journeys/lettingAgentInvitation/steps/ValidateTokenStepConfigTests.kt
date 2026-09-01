package uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.lettingAgentInvitation.LettingAgentInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

@ExtendWith(MockitoExtension::class)
class ValidateTokenStepConfigTests {
    @Mock
    lateinit var mockLettingAgentAccessService: LettingAgentAccessService

    @Mock
    lateinit var mockState: LettingAgentInvitationJourneyState

    @Nested
    inner class Mode {
        @Test
        fun `mode returns VALID when token exists in the database`() {
            val stepConfig = ValidateTokenStepConfig(mockLettingAgentAccessService)
            whenever(mockState.invitationToken).thenReturn("some-token")
            whenever(mockLettingAgentAccessService.getTokenIsValid("some-token")).thenReturn(true)

            val result = stepConfig.mode(mockState)

            assertEquals(TokenValidationResult.VALID, result)
        }

        @Test
        fun `mode returns INVALID when token does not exist in the database`() {
            val stepConfig = ValidateTokenStepConfig(mockLettingAgentAccessService)
            whenever(mockState.invitationToken).thenReturn("some-token")
            whenever(mockLettingAgentAccessService.getTokenIsValid("some-token")).thenReturn(false)

            val result = stepConfig.mode(mockState)

            assertEquals(TokenValidationResult.INVALID, result)
        }

        @Test
        fun `mode returns null when invitationToken is null`() {
            val stepConfig = ValidateTokenStepConfig(mockLettingAgentAccessService)
            whenever(mockState.invitationToken).thenReturn(null)

            val result = stepConfig.mode(mockState)

            assertNull(result)
        }
    }

    @Nested
    inner class AfterStepIsReached {
        private val journeyId = "test-journey-id"
        private val token = "some-token"

        @Test
        fun `afterStepIsReached stores the invitation token in state`() {
            val stepConfig = ValidateTokenStepConfig(mockLettingAgentAccessService)
            whenever(mockState.journeyId).thenReturn(journeyId)
            whenever(mockLettingAgentAccessService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)

            stepConfig.afterStepIsReached(mockState)

            verify(mockState).invitationToken = token
        }
    }
}
