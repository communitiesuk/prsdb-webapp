package uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS
import uk.gov.communities.prsdb.webapp.journeys.acceptOrganisationalLandlordInvitation.AcceptOrganisationalLandlordUserInvitationJourneyState
import uk.gov.communities.prsdb.webapp.services.OrganisationalLandlordInvitationService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class ValidateTokenStepConfigTests {
    @Mock
    lateinit var mockInvitationService: OrganisationalLandlordInvitationService

    @Mock
    lateinit var mockState: AcceptOrganisationalLandlordUserInvitationJourneyState

    @Test
    fun `afterStepIsReached records a valid token and caches the organisation name for a pending invitation`() {
        val organisationalLandlord = MockLandlordData.createOrgLandlord(name = "Test Organisation")
        val invitation =
            MockLandlordData.createOrganisationalLandlordInvitation(organisationalLandlord = organisationalLandlord)
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(invitation)

        ValidateTokenStepConfig(mockInvitationService).afterStepIsReached(mockState)

        verify(mockState).tokenIsValid = true
        verify(mockState).organisationName = "Test Organisation"
    }

    @Test
    fun `afterStepIsReached records an invalid token when no invitation is found for the journey`() {
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(null)

        ValidateTokenStepConfig(mockInvitationService).afterStepIsReached(mockState)

        verify(mockState).tokenIsValid = false
        verify(mockState).organisationName = null
    }

    @Test
    fun `afterStepIsReached records an invalid token for an expired invitation`() {
        val invitation =
            MockLandlordData.createOrganisationalLandlordInvitation(
                createdDate =
                    Instant.now().minus(
                        (ORGANISATIONAL_LANDLORD_INVITATION_LIFETIME_IN_DAYS + 1).toLong(),
                        ChronoUnit.DAYS,
                    ),
            )
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(invitation)

        ValidateTokenStepConfig(mockInvitationService).afterStepIsReached(mockState)

        verify(mockState).tokenIsValid = false
        verify(mockState).organisationName = null
    }

    @Test
    fun `afterStepIsReached records an invalid token for a hidden invitation`() {
        val invitation = MockLandlordData.createOrganisationalLandlordInvitation(isHidden = true)
        whenever(mockState.journeyId).thenReturn(JOURNEY_ID)
        whenever(mockInvitationService.getInvitationForJourneyIdOrNull(JOURNEY_ID)).thenReturn(invitation)

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
