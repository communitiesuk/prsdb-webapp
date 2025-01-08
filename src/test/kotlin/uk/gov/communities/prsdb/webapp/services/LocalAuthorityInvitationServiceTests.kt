package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpSession
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.InvalidTokenException
import java.util.UUID

class LocalAuthorityInvitationServiceTests {
    private lateinit var mockLaInviteRepository: LocalAuthorityInvitationRepository
    private lateinit var inviteService: LocalAuthorityInvitationService
    private lateinit var journeyDataService: JourneyDataService
    private lateinit var session: HttpSession

    @BeforeEach
    fun setup() {
        mockLaInviteRepository = mock()
        session = MockHttpSession()
        inviteService = LocalAuthorityInvitationService(mockLaInviteRepository, session)
        journeyDataService = mock()
    }

    @Test
    fun `createInviteToken saves the created invite token for the given authority`() {
        val authority = LocalAuthority()

        val token = inviteService.createInvitationToken("email", authority)

        val inviteCaptor = captor<LocalAuthorityInvitation>()
        verify(mockLaInviteRepository).save(inviteCaptor.capture())

        assertEquals(authority, inviteCaptor.value.invitingAuthority)
        assertEquals(token, inviteCaptor.value.token.toString())
    }

    @Test
    fun `getAuthorityForToken returns the authority the token was created with`() {
        val testUuid = UUID.randomUUID()
        val testEmail = "test@example.com"
        val testAuthority = LocalAuthority()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(LocalAuthorityInvitation(testUuid, testEmail, testAuthority))

        val authority = inviteService.getAuthorityForToken(testUuid.toString())

        assertEquals(authority, testAuthority)
    }

    @Test
    fun `getEmailAddressForToken returns the email the invitation was send to`() {
        val testUuid = UUID.randomUUID()
        val testEmail = "test@example.com"
        val testAuthority = LocalAuthority()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(LocalAuthorityInvitation(testUuid, testEmail, testAuthority))

        val email = inviteService.getEmailAddressForToken(testUuid.toString())

        assertEquals(email, testEmail)
    }

    @Test
    fun `getInvitationFromToken returns an invitation if the token is in the database`() {
        val testUuid = UUID.randomUUID()
        val testEmail = "test@example.com"
        val testAuthority = LocalAuthority()
        val testInvitation = LocalAuthorityInvitation(testUuid, testEmail, testAuthority)
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(testInvitation)

        val invitation = inviteService.getInvitationFromToken(testUuid.toString())

        assertEquals(invitation, testInvitation)
    }

    @Test
    fun `getInvitationFromToken throws an exception if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(null)

        val thrown = assertThrows(InvalidTokenException::class.java) { inviteService.getInvitationFromToken(testUuid.toString()) }
        assertEquals("Token not found in database", thrown.message)
    }

    @Test
    fun `tokenIsValid returns true if the token is in the database`() {
        val testUuid = UUID.randomUUID()
        val testEmail = "test@example.com"
        val testAuthority = LocalAuthority()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(LocalAuthorityInvitation(testUuid, testEmail, testAuthority))

        assertTrue(inviteService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `tokenIsValid returns false if the token is not in the database`() {
        val testUuid = UUID.randomUUID()
        whenever(mockLaInviteRepository.findByToken(testUuid)).thenReturn(null)

        assertFalse(inviteService.tokenIsValid(testUuid.toString()))
    }

    @Test
    fun `getInvitationById returns an invitation if the id is in the database`() {
        val testId = 123.toLong()
        val invitationFromDatabase =
            LocalAuthorityInvitation(
                UUID.randomUUID(),
                "test@example.com",
                LocalAuthority(),
            )

        whenever(mockLaInviteRepository.getById(testId)).thenReturn(invitationFromDatabase)

        assertEquals(invitationFromDatabase, inviteService.getInvitationById(testId))
    }
}
