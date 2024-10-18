package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor.captor
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthority
import uk.gov.communities.prsdb.webapp.database.entity.LocalAuthorityInvite
import uk.gov.communities.prsdb.webapp.database.repository.LocalAuthorityInviteRepository
import java.util.UUID

class LocalAuthorityInviteServiceTests {
    private lateinit var mockLaInviteRepository: LocalAuthorityInviteRepository
    private lateinit var inviteService: LocalAuthorityInviteService

    @BeforeEach
    fun setup() {
        mockLaInviteRepository = mock()
        inviteService = LocalAuthorityInviteService(mockLaInviteRepository)
    }

    @Test
    fun `createInviteToken saves the created invite token for the given authority`() {
        val authority = LocalAuthority()

        val token = inviteService.createInviteToken(authority)

        val inviteCaptor = captor<LocalAuthorityInvite>()
        verify(mockLaInviteRepository).save(inviteCaptor.capture())

        assertEquals(authority, inviteCaptor.value.invitingAuthority)
        assertEquals(token, inviteCaptor.value.token.toString())
    }

    @Test
    fun `getAuthorityForToken returns the authority the token was created with`() {
        val testUuid = UUID.randomUUID()
        val testAuthority = LocalAuthority()
        `when`(mockLaInviteRepository.findByToken(testUuid)).thenReturn(LocalAuthorityInvite(testUuid, testAuthority))

        val authority = inviteService.getAuthorityForToken(testUuid.toString())

        assertEquals(authority, testAuthority)
    }
}
