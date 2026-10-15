package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.ORGANISATIONAL_LANDLORD_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.enums.OrganisationalLandlordInvitationStatus
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlordInvitation
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordInvitationRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrgLandlord
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createOrganisationalLandlordInvitation
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
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
    fun `getInvitationsForOrganisation returns the organisation's invitations from the repository`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitations = listOf(createOrganisationalLandlordInvitation(organisationalLandlord = organisation))
        whenever(mockInvitationRepository.findByOrganisationalLandlord(organisation)).thenReturn(invitations)

        // Act
        val result = invitationService.getInvitationsForOrganisation(organisation)

        // Assert
        assertEquals(invitations, result)
    }

    @Test
    fun `hideExpiredInvitation hides and saves an expired invitation`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitation =
            createOrganisationalLandlordInvitation(
                id = 1,
                organisationalLandlord = organisation,
                createdDate = Instant.now().minus(60, ChronoUnit.DAYS),
            )
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act
        invitationService.hideExpiredInvitation(1, organisation)

        // Assert
        assertTrue(invitation.isHidden)
        assertEquals(OrganisationalLandlordInvitationStatus.HIDDEN, invitation.status)
        verify(mockInvitationRepository).save(invitation)
    }

    @Test
    fun `hideExpiredInvitation throws 404 when the invitation does not exist`() {
        // Arrange
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.empty())

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, createOrgLandlord())
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
    }

    @Test
    fun `hideExpiredInvitation throws 403 when the invitation belongs to another organisation`() {
        // Arrange
        val otherOrganisation = createOrgLandlord(name = "Other organisation")
        val invitation =
            createOrganisationalLandlordInvitation(
                id = 1,
                organisationalLandlord = otherOrganisation,
                createdDate = Instant.now().minus(60, ChronoUnit.DAYS),
            )
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, createOrgLandlord())
            }
        assertEquals(HttpStatus.FORBIDDEN, exception.statusCode)
        verify(mockInvitationRepository, never()).save(any())
    }

    @Test
    fun `hideExpiredInvitation throws 400 when the invitation is still pending`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitation =
            createOrganisationalLandlordInvitation(id = 1, organisationalLandlord = organisation)
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, organisation)
            }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verify(mockInvitationRepository, never()).save(any())
    }

    @Test
    fun `hideExpiredInvitation throws 400 when the invitation is already hidden`() {
        // Arrange
        val organisation = createOrgLandlord()
        val invitation =
            createOrganisationalLandlordInvitation(
                id = 1,
                organisationalLandlord = organisation,
                createdDate = Instant.now().minus(60, ChronoUnit.DAYS),
                isHidden = true,
            )
        whenever(mockInvitationRepository.findById(1)).thenReturn(Optional.of(invitation))

        // Act, Assert
        val exception =
            assertThrows<ResponseStatusException> {
                invitationService.hideExpiredInvitation(1, organisation)
            }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        verify(mockInvitationRepository, never()).save(any())
    }
}
