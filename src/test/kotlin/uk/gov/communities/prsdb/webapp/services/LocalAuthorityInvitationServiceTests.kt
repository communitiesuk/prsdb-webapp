package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
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
}
