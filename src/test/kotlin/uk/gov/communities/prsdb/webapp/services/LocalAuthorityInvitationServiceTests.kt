package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
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
import uk.gov.communities.prsdb.webapp.constants.LOCAL_AUTHORITY_INVITATION_LIFETIME_IN_HOURS
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvitation
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInvitationRepository
import uk.gov.communities.prsdb.webapp.exceptions.TokenNotFoundException
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLocalAuthorityData
import java.util.UUID
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

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
        val authority = MockLocalAuthorityData.createLocalAuthority()

        val token = inviteService.createInvitationToken("email", authority)

        val inviteCaptor = captor<LocalAuthorityInvitation>()
        verify(mockLaInviteRepository).save(inviteCaptor.capture())

        assertEquals(authority, inviteCaptor.value.invitingAuthority)
        assertEquals(token, inviteCaptor.value.token.toString())
    }

    @Test
    fun `getAuthorityForToken returns the authority the token was created with`() {
        val testUuid = UUID.randomUUID()
        val testAuthority = MockLocalAuthorityData.createLocalAuthority(id = 789)

        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(MockLocalAuthorityData.createLocalAuthorityInvitation(invitingAuthority = testAuthority, token = testUuid))

        val authority = inviteService.getAuthorityForToken(testUuid.toString())

        assertEquals(authority, testAuthority)
    }

    @Test
    fun `getEmailAddressForToken returns the email the invitation was send to`() {
        val testUuid = UUID.randomUUID()
        val testEmail = "test@example.com"
        whenever(mockLaInviteRepository.findByToken(testUuid))
            .thenReturn(MockLocalAuthorityData.createLocalAuthorityInvitation(email = testEmail, token = testUuid))

        val email = inviteService.getEmailAddressForToken(testUuid.toString())

        assertEquals(email, testEmail)
    }

    @Test
    fun `getInvitationFromToken returns an invitation if the token is in the database`() {
        val testUuid = UUID.randomUUID()
        val testInvitation = MockLocalAuthorityData.createLocalAuthorityInvitation(token = testUuid)
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
    fun `getInvitationHasExpired returns true if the invitation has expired`() {
        val testUuid = UUID.randomUUID()
        val createdDate =
            Clock.System
                .now()
                .minus(LOCAL_AUTHORITY_INVITATION_LIFETIME_IN_HOURS.hours)
                .minus(1.minutes)
                .toJavaInstant()

        val invitation = MockLocalAuthorityData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate)

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

        val invitation = MockLocalAuthorityData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate)

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
            .thenReturn(MockLocalAuthorityData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate))

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
            .thenReturn(MockLocalAuthorityData.createLocalAuthorityInvitation(token = testUuid, createdDate = createdDate))

        assertFalse(inviteService.tokenIsValid(testUuid.toString()))
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
        val invitationFromDatabase = MockLocalAuthorityData.createLocalAuthorityInvitation(id = testId)

        whenever(mockLaInviteRepository.getReferenceById(testId)).thenReturn(invitationFromDatabase)

        assertEquals(invitationFromDatabase, inviteService.getInvitationById(testId))
    }
}
