package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService

@ExtendWith(MockitoExtension::class)
class ValidateTokenStepConfigTests {
    @Mock
    lateinit var mockInvitationRepository: JointLandlordInvitationRepository

    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    @Nested
    inner class Mode {
        @Test
        fun `mode returns VALID when tokenIsValid is true`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.tokenIsValid).thenReturn(true)

            val result = stepConfig.mode(mockState)

            assertEquals(TokenValidationResult.VALID, result)
        }

        @Test
        fun `mode returns INVALID when tokenIsValid is false`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.tokenIsValid).thenReturn(false)

            val result = stepConfig.mode(mockState)

            assertEquals(TokenValidationResult.INVALID, result)
        }

        @Test
        fun `mode returns null when tokenIsValid is null`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.tokenIsValid).thenReturn(null)

            val result = stepConfig.mode(mockState)

            assertNull(result)
        }
    }

    @Nested
    inner class AfterStepIsReached {
        private val journeyId = "test-journey-id"
        private val token = "some-token"

        @ParameterizedTest
        @ValueSource(booleans = [true, false])
        fun `afterStepIsReached sets tokenIsValid to the result of getTokenIsValid`(tokenIsValid: Boolean) {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.journeyId).thenReturn(journeyId)
            whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(token)
            whenever(mockInvitationService.getTokenIsValid(token)).thenReturn(tokenIsValid)

            stepConfig.afterStepIsReached(mockState)

            verify(mockState).tokenIsValid = tokenIsValid
        }

        @Test
        fun `afterStepIsReached throws PrsdbWebException when token is not found in session`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.journeyId).thenReturn(journeyId)
            whenever(mockInvitationService.getInvitationTokenForJourneyIdFromSession(journeyId)).thenReturn(null)

            assertThrows<PrsdbWebException> {
                stepConfig.afterStepIsReached(mockState)
            }
        }
    }
}
