package uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.steps

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.JointLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.JointLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.journeys.acceptOrRejectJointLandlordInvitation.AcceptOrRejectJointLandlordInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.JointLandlordInvitationService
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ValidateTokenStepConfigTests {
    @Mock
    lateinit var mockInvitationRepository: JointLandlordInvitationRepository

    @Mock
    lateinit var mockInvitationService: JointLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptOrRejectJointLandlordInvitationJourneyState

    @Mock
    lateinit var mockInvitation: JointLandlordInvitation

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
        private val validToken = UUID.randomUUID().toString()

        @Test
        fun `afterStepIsReached sets tokenIsValid to true when token is a valid unexpired invitation`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.invitationToken).thenReturn(validToken)
            whenever(mockInvitationRepository.findByToken(UUID.fromString(validToken))).thenReturn(mockInvitation)
            whenever(mockInvitationService.getInvitationHasExpired(mockInvitation)).thenReturn(false)

            stepConfig.afterStepIsReached(mockState)

            verify(mockState).tokenIsValid = true
        }

        @Test
        fun `afterStepIsReached sets tokenIsValid to false when token is not a valid UUID`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.invitationToken).thenReturn("not-a-valid-uuid")

            stepConfig.afterStepIsReached(mockState)

            verify(mockState).tokenIsValid = false
        }

        @Test
        fun `afterStepIsReached sets tokenIsValid to false when token is not found in the repository`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.invitationToken).thenReturn(validToken)
            whenever(mockInvitationRepository.findByToken(UUID.fromString(validToken))).thenReturn(null)

            stepConfig.afterStepIsReached(mockState)

            verify(mockState).tokenIsValid = false
        }

        @Test
        fun `afterStepIsReached sets tokenIsValid to false when invitation has expired`() {
            val stepConfig = ValidateTokenStepConfig(mockInvitationRepository, mockInvitationService)
            whenever(mockState.invitationToken).thenReturn(validToken)
            whenever(mockInvitationRepository.findByToken(UUID.fromString(validToken))).thenReturn(mockInvitation)
            whenever(mockInvitationService.getInvitationHasExpired(mockInvitation)).thenReturn(true)

            stepConfig.afterStepIsReached(mockState)

            verify(mockState).tokenIsValid = false
        }
    }
}
