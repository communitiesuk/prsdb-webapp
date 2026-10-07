package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole.EDITOR
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class OrganisationalLandlordInvitationServiceTests {
    @Mock
    private lateinit var mockInvitationRepository: OrganisationalLandlordInvitationRepository

    @Mock
    private lateinit var mockHttpSession: HttpSession

    private lateinit var invitationService: OrganisationalLandlordInvitationService

    private val journeyId = "test-journey-id"
    private val token = UUID.fromString("1234abcd-5678-abcd-1234-567abcd2222a")

    @BeforeEach
    fun setup() {
        invitationService = OrganisationalLandlordInvitationService(mockInvitationRepository, mockHttpSession)
    }

    @Test
    fun `addJourneyIdInvitationTokenPairToSession adds the journey id and invitation token to the session`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(null)

        // Act
        invitationService.addJourneyIdInvitationTokenPairToSession(journeyId, token.toString())

        // Assert
        verify(mockHttpSession)
            .setAttribute(
                ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS,
                mutableListOf(journeyId to token.toString()),
            )
    }

    @Test
    fun `addJourneyIdInvitationTokenPairToSession preserves existing pairs in the session`() {
        // Arrange
        val existingPair = "existing-journey-id" to "existing-token"
        val existingPairs = mutableListOf(existingPair)
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(existingPairs)

        // Act
        invitationService.addJourneyIdInvitationTokenPairToSession(journeyId, token.toString())

        // Assert
        verify(mockHttpSession)
            .setAttribute(
                ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS,
                mutableListOf(existingPair, journeyId to token.toString()),
            )
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns the invitation for a token associated with the journey`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        val invitation = org.mockito.kotlin.mock<OrganisationalLandlordInvitation>()
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(invitation)

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertEquals(invitation, result)
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns null when the journey has no stored token`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(null)

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
        verifyNoInteractions(mockInvitationRepository)
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns null when the stored token is malformed`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to "not-a-uuid"))

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
        verifyNoInteractions(mockInvitationRepository)
    }

    @Test
    fun `getInvitationForJourneyIdOrNull returns null when no invitation matches the stored token`() {
        // Arrange
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(null)

        // Act
        val result = invitationService.getInvitationForJourneyIdOrNull(journeyId)

        // Assert
        assertNull(result)
    }

    @Test
    fun `createInvitation saves an invitation for the organisation with the given email, role and a token`() {
        // Arrange
        val organisation = createOrgLandlord()

        // Act
        invitationService.createInvitation("invitee@example.com", EDITOR, organisation)

        // Assert
        val invitationCaptor = argumentCaptor<OrganisationalLandlordInvitation>()
        verify(mockInvitationRepository).save(invitationCaptor.capture())
        val savedInvitation = invitationCaptor.firstValue
        assertEquals("invitee@example.com", savedInvitation.invitedEmail)
        assertEquals(EDITOR, savedInvitation.role)
        assertEquals(organisation, savedInvitation.organisationalLandlord)
        assertNotNull(savedInvitation.token)
    }
}
