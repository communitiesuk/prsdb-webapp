package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ValidateTokenStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    @Test
    fun `afterStepIsReached records a valid token and caches the organisation name for a valid invitation`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getOrganisationNameForInvitationIfValid(JOURNEY_ID))
            .thenReturn("Test Organisation")

        ValidateTokenStepConfig(mockInvitationService).afterStepIsReached(mockState)

        verify(mockState).tokenIsValid = true
        verify(mockState).organisationName = "Test Organisation"
    }

    @Test
    fun `afterStepIsReached records an invalid token when there is no valid invitation for the journey`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getOrganisationNameForInvitationIfValid(JOURNEY_ID)).thenReturn(null)

        ValidateTokenStepConfig(mockInvitationService).afterStepIsReached(mockState)

        verify(mockState).tokenIsValid = false
        verify(mockState).organisationName = null
    }

    @Test
    fun `mode returns VALID when the token has been validated successfully`() {
        whenever(mockState.tokenIsValid).thenReturn(true)

        assertEquals(TokenValidity.VALID, ValidateTokenStepConfig(mockInvitationService).mode(mockState))
    }

    @Test
    fun `mode returns INVALID when the token has been rejected`() {
        whenever(mockState.tokenIsValid).thenReturn(false)

        assertEquals(TokenValidity.INVALID, ValidateTokenStepConfig(mockInvitationService).mode(mockState))
    }

    @Test
    fun `mode returns null when the token has not yet been validated`() {
        whenever(mockState.tokenIsValid).thenReturn(null)

        assertNull(ValidateTokenStepConfig(mockInvitationService).mode(mockState))
    }

    companion object {
        private const val JOURNEY_ID = "test-journey-id"
    }
}
