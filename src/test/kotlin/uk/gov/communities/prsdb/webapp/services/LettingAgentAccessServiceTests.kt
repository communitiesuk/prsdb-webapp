package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class LettingAgentAccessServiceTests {
    @Mock
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository

    @InjectMocks
    private lateinit var lettingAgentAccessService: LettingAgentAccessService

    @Test
    fun `createInvitation generates a token and saves the invitation`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership()
        whenever(lettingAgentAccessRepository.save(any<LettingAgentAccess>())).thenAnswer { it.arguments[0] }

        val result = lettingAgentAccessService.createInvitation(propertyOwnership, "letting.agent@example.com")

        assertNotNull(result.token)
        assertEquals("letting.agent@example.com", result.invitedEmail)
        assertEquals(propertyOwnership, result.propertyOwnership)
        verify(lettingAgentAccessRepository).save(result)
    }

    @Test
    fun `getInvitationByToken returns the invitation when it exists`() {
        val token = UUID.randomUUID()
        val invitation = MockLettingAgentData.createLettingAgentAccess(token = token)
        whenever(lettingAgentAccessRepository.findByToken(token)).thenReturn(invitation)

        val result = lettingAgentAccessService.getInvitationByToken(token)

        assertEquals(invitation, result)
    }

    @Test
    fun `getInvitationByToken throws EntityNotFoundException when no invitation exists`() {
        val token = UUID.randomUUID()
        whenever(lettingAgentAccessRepository.findByToken(token)).thenReturn(null)

        assertThrows<EntityNotFoundException> {
            lettingAgentAccessService.getInvitationByToken(token)
        }
    }

    @Test
    fun `deleteDelegationByPropertyOwnershipId deletes the delegation`() {
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(1L)

        verify(lettingAgentAccessRepository).deleteByPropertyOwnershipId(1L)
    }

    @Test
    fun `getInvitationByTokenOrNull returns the invitation when it exists`() {
        val token = UUID.randomUUID()
        val invitation = MockLettingAgentData.createLettingAgentAccess(token = token)
        whenever(lettingAgentAccessRepository.findByToken(token)).thenReturn(invitation)

        assertEquals(invitation, lettingAgentAccessService.getInvitationByTokenOrNull(token))
    }

    @Test
    fun `getInvitationByTokenOrNull returns null when no invitation exists`() {
        val token = UUID.randomUUID()
        whenever(lettingAgentAccessRepository.findByToken(token)).thenReturn(null)

        assertNull(lettingAgentAccessService.getInvitationByTokenOrNull(token))
    }

    @Nested
    inner class AuthorisedAccessTokens {
        @Test
        fun `getAccessIdForTokenOrNull returns null for a non-UUID string`() {
            assertNull(lettingAgentAccessService.getAccessIdForTokenOrNull("not-a-uuid"))
        }

        @Test
        fun `getAccessIdForTokenOrNull returns null when no invitation exists for the token`() {
            val token = UUID.randomUUID()
            whenever(lettingAgentAccessRepository.findByToken(token)).thenReturn(null)

            assertNull(lettingAgentAccessService.getAccessIdForTokenOrNull(token.toString()))
        }

        @Test
        fun `getAccessIdForTokenOrNull returns the access id for a known invitation token`() {
            val token = UUID.randomUUID()
            whenever(lettingAgentAccessRepository.findByToken(token))
                .thenReturn(MockLettingAgentData.createLettingAgentAccess(id = 42L, token = token))

            assertEquals(42L, lettingAgentAccessService.getAccessIdForTokenOrNull(token.toString()))
        }
    }
}
