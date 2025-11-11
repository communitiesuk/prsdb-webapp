package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpSession
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_INVITATION_LIFETIME_IN_HOURS
import uk.gov.communities.prsdb.webapp.database.entity.LocalCouncilInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.TokenNotFoundException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalCouncilData
import java.util.Optional
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class LocalCouncilInvitationServiceTests {
    private lateinit var mockLaInviteRepository: LocalAuthorityInvitationRepository
    private lateinit var inviteService: LocalCouncilInvitationService
    private lateinit var journeyDataService: JourneyDataService
    private lateinit var session: HttpSession

    @BeforeEach
    fun setup() {
        mockLaInviteRepository = mock()
        session = MockHttpSession()
        inviteService = LocalCouncilInvitationService(mockLaInviteRepository, session)
        journeyDataService = mock()
    }

    @Test
    fun `createInviteToken saves the created invite token for the given authority`() {
        val authority = MockLocalCouncilData.createLocalAuthority()

        val token = inviteService.createInvitationToken("email", authority)

        val inviteCaptor = captor<LocalCouncilInvitation>()
        verify(mockLaInviteRepository).save(inviteCaptor.capture())

        assertEquals(authority, inviteCaptor.value.invitingAuthority)
        assertEquals(token, inviteCaptor.value.token.toString())
    }

    @Test
    fun `getAuthorityForToken returns the authority the token was created with`() {
        val testUuid = UUID.randomUUID()
        val testAuthority = MockLocalCouncilData.createLocalAuthority(id = 789)

        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(MockLocalCouncilData.createLocalAuthorityInvitation(invitingAuthority = testAuthority, token = testUuid))

        val authority = inviteService.getAuthorityForToken(testUuid.toString())

        assertEquals(authority, testAuthority)
    }

    @Test
    fun `getEmailAddressForToken returns the email the invitation was send to`() {
        val testUuid = UUID.randomUUID()
        val testEmail = "test@example.com"
        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(MockLocalCouncilData.createLocalAuthorityInvitation(email = testEmail, token = testUuid))

        val email = inviteService.getEmailAddressForToken(testUuid.toString())

        assertEquals(email, testEmail)
    }

    @Test
    fun `getInvitationFromToken returns an invitation if the token is in the database`() {
        val testUuid = UUID.randomUUID()
        val testInvitation = MockLocalCouncilData.createLocalAuthorityInvitation(token = testUuid)
        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(testInvitation)

        val invitation = inviteService.getInvitationFromToken(testUuid.toString())

        assertEquals(invitation, testInvitation)
    }

    @Test
    fun `getInvitationFromToken throws an exception if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(null)

        val thrown = assertThrows(TokenNotFoundException::class.java) { inviteService.getInvitationFromToken(testUuid.toString()) }
        assertEquals("Invitation token not found in database", thrown.message)
    }

    @Test
    fun `getInvitationOrNull returns an invitation if the token is in the database`() {
        val testUuid = UUID.randomUUID()
        val testInvitation = MockLocalCouncilData.createLocalAuthorityInvitation(token = testUuid)
        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(testInvitation)

        val invitation = inviteService.getInvitationOrNull(testUuid.toString())

        assertEquals(invitation, testInvitation)
    }

    @Test
    fun `getInvitationOrNull returns null if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(null)

        val invitation = inviteService.getInvitationOrNull(testUuid.toString())
        assertNull(invitation)
    }

    @Test
    fun `getInvitationHasExpired returns true if the invitation has expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(LOCAL_AUTHORITY_INVITATION_LIFETIME_IN_HOURS.hours)
                .minus(1.minutes)
                .toJavaInstant()

        val invitation = MockLocalCouncilData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate)

        assertTrue(inviteService.getInvitationHasExpired(invitation))
    }

    @Test
    fun `getInvitationHasExpired returns false if the invitation has not expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(LOCAL_AUTHORITY_INVITATION_LIFETIME_IN_HOURS.hours)
                .plus(30.minutes)
                .toJavaInstant()

        val invitation = MockLocalCouncilData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate)

        assertFalse(inviteService.getInvitationHasExpired(invitation))
    }

    @Test
    fun `tokenIsValid returns true if the token is in the database and has not expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(LOCAL_AUTHORITY_INVITATION_LIFETIME_IN_HOURS.hours)
                .plus(30.minutes)
                .toJavaInstant()
        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(MockLocalCouncilData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate))

        assertTrue(inviteService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `tokenIsValid returns false if the token is in the database but has expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(LOCAL_AUTHORITY_INVITATION_LIFETIME_IN_HOURS.hours)
                .minus(1.minutes)
                .toJavaInstant()

        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(MockLocalCouncilData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate))

        assertFalse(inviteService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `tokenIsValid returns false if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(null)

        assertFalse(inviteService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `getInvitationByIdOrNull returns an invitation if the id is in the database`() {
        val testId = 123.toLong()
        val invitationFromDatabase = MockLocalCouncilData.createLocalAuthorityInvitation(id = testId)

        whenever(mockLaInviteRepository.findById(testId))
            .thenReturn(Optional.of(invitationFromDatabase) as Optional<LocalCouncilInvitation?>)

        assertEquals(invitationFromDatabase, inviteService.getInvitationByIdOrNull(testId))
    }

    @Test
    fun `getInvitationByIdOrNull returns null if the id is no in the database`() {
        val testId = 123.toLong()

        assertNull(inviteService.getInvitationByIdOrNull(testId))
    }

    @Test
    fun `getAdminInvitationByIdOrNull returns an invitation if the id is in the database and it is admin`() {
        val invitation = MockLocalCouncilData.createLocalAuthorityInvitation(invitedAsAdmin = true)

        whenever(mockLaInviteRepository.findById(invitation.id)).thenReturn(Optional.of(invitation) as Optional<LocalCouncilInvitation?>)

        val result = inviteService.getAdminInvitationByIdOrNull(invitation.id)

        assertEquals(invitation, result)
    }

    @Test
    fun `getAdminInvitationByIdOrNull returns null if the id is not in the database`() {
        val notExistingInvitationId = 123.toLong()

        val result = inviteService.getAdminInvitationByIdOrNull(notExistingInvitationId)

        assertNull(result)
    }

    @Test
    fun `getAdminInvitationByIdOrNull returns an null if the id is in the database and it is NOT admin`() {
        val invitation = MockLocalCouncilData.createLocalAuthorityInvitation(invitedAsAdmin = false)

        whenever(mockLaInviteRepository.findById(invitation.id)).thenReturn(Optional.of(invitation) as Optional<LocalCouncilInvitation?>)

        val result = inviteService.getAdminInvitationByIdOrNull(invitation.id)

        assertNull(result)
    }

    @Test
    fun `throwErrorIfInvitationExists does not throws error if invitation doesn't exist`() {
        // Arrange
        val invitation = MockLocalCouncilData.createLocalAuthorityInvitation()
        whenever(mockLaInviteRepository.existsById(invitation.id)).thenReturn(false)

        // Act and Assert
        org.junit.jupiter.api.assertDoesNotThrow {
            inviteService.throwErrorIfInvitationExists(invitation)
        }
    }

    @Test
    fun `throwErrorIfInvitationExists throws INTERNAL SERVER ERROR if invitation still exists`() {
        // Arrange
        val invitation = MockLocalCouncilData.createLocalAuthorityInvitation()
        whenever(mockLaInviteRepository.existsById(invitation.id)).thenReturn(true)

        // Act and Assert
        val errorThrown =
            org.junit.jupiter.api.assertThrows<ResponseStatusException> {
                inviteService.throwErrorIfInvitationExists(invitation)
            }
        kotlin.test.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, errorThrown.statusCode)
    }
}
