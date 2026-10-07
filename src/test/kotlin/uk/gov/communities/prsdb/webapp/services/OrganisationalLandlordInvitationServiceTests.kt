package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordUserRole
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class OrganisationalLandlordInvitationServiceTests {
    @Mock
    private lateinit var mockInvitationRepository: OrganisationalLandlordInvitationRepository

    @Mock
    private lateinit var mockHttpSession: HttpSession

    @Mock
    private lateinit var mockOrganisationalLandlordUserService: OrganisationalLandlordUserService

    @Mock
    private lateinit var mockPrsdbUserService: PrsdbUserService

    private lateinit var invitationService: OrganisationalLandlordInvitationService

    private val journeyId = "test-journey-id"
    private val token = UUID.fromString("1234abcd-5678-abcd-1234-567abcd2222a")

    @BeforeEach
    fun setup() {
        invitationService =
            OrganisationalLandlordInvitationService(
                mockInvitationRepository,
                mockHttpSession,
                mockOrganisationalLandlordUserService,
                mockPrsdbUserService,
            )
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
    fun `clearJourneyIdInvitationTokenPairsForTokenFromSession removes only pairs for the accepted token`() {
        // Arrange
        val unrelatedPair = "unrelated-journey-id" to "unrelated-token"
        val sessionPairs =
            mutableListOf(
                journeyId to token.toString(),
                "another-journey-id" to token.toString(),
                unrelatedPair,
            )
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(sessionPairs)

        // Act
        invitationService.clearJourneyIdInvitationTokenPairsForTokenFromSession(token.toString())

        // Assert
        verify(mockHttpSession)
            .setAttribute(
                ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS,
                mutableListOf(unrelatedPair),
            )
    }

    @Test
    fun `acceptInvitation saves a user using the invitation organisation and role then deletes the invitation`() {
        // Arrange
        val baseUserId = "signed-in-user"
        val name = "Jane Smith"
        val email = "invitee@example.com"
        val baseUser = PrsdbUser(baseUserId)
        val invitedLandlord = mock<OrganisationalLandlord>()
        val invitation = mock<OrganisationalLandlordInvitation>()
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(invitation)
        whenever(invitation.organisationalLandlord).thenReturn(invitedLandlord)
        whenever(invitation.role).thenReturn(OrganisationalLandlordUserRole.EDITOR)
        whenever(invitation.token).thenReturn(token)
        whenever(mockPrsdbUserService.findOrCreatePrsdbUser(baseUserId)).thenReturn(baseUser)
        whenever(
            mockOrganisationalLandlordUserService.createOrganisationalLandlordUser(
                invitedLandlord,
                baseUser,
                name,
                email,
                OrganisationalLandlordUserRole.EDITOR,
            ),
        ).thenReturn(mock())

        // Act
        val acceptedToken = invitationService.acceptInvitation(journeyId, baseUserId, name, email)

        // Assert
        assertEquals(token.toString(), acceptedToken)
        verify(mockOrganisationalLandlordUserService)
            .createOrganisationalLandlordUser(
                invitedLandlord,
                baseUser,
                name,
                email,
                OrganisationalLandlordUserRole.EDITOR,
            )
        verify(mockInvitationRepository).delete(invitation)
    }

    @Test
    fun `acceptInvitation does not delete the invitation when saving the user fails`() {
        // Arrange
        val baseUserId = "signed-in-user"
        val name = "Jane Smith"
        val email = "invitee@example.com"
        val baseUser = PrsdbUser(baseUserId)
        val invitedLandlord = mock<OrganisationalLandlord>()
        val invitation = mock<OrganisationalLandlordInvitation>()
        whenever(mockHttpSession.getAttribute(ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS))
            .thenReturn(mutableListOf(journeyId to token.toString()))
        whenever(mockInvitationRepository.findByToken(token)).thenReturn(invitation)
        whenever(invitation.organisationalLandlord).thenReturn(invitedLandlord)
        whenever(invitation.role).thenReturn(OrganisationalLandlordUserRole.ADMIN)
        whenever(mockPrsdbUserService.findOrCreatePrsdbUser(baseUserId)).thenReturn(baseUser)
        whenever(
            mockOrganisationalLandlordUserService.createOrganisationalLandlordUser(
                invitedLandlord,
                baseUser,
                name,
                email,
                OrganisationalLandlordUserRole.ADMIN,
            ),
        ).thenThrow(IllegalStateException("User save failed"))

        // Act / Assert
        assertThrows(IllegalStateException::class.java) {
            invitationService.acceptInvitation(journeyId, baseUserId, name, email)
        }
        verify(mockInvitationRepository, never()).delete(invitation)
    }
}
