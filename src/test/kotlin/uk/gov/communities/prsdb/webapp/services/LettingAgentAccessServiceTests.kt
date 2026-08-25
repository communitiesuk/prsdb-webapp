package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLettingAgentData
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class LettingAgentAccessServiceTests {
    @Mock
    private lateinit var lettingAgentAccessRepository: LettingAgentAccessRepository

    @Mock
    private lateinit var session: HttpSession

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
    fun `getInvitationByPropertyOwnershipId returns the invitation when it exists`() {
        val invitation = MockLettingAgentData.createLettingAgentAccess()
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(1L)).thenReturn(invitation)

        val result = lettingAgentAccessService.getInvitationByPropertyOwnershipId(1L)

        assertEquals(invitation, result)
    }

    @Test
    fun `getInvitationByPropertyOwnershipId returns null when no invitation exists`() {
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(1L)).thenReturn(null)

        val result = lettingAgentAccessService.getInvitationByPropertyOwnershipId(1L)

        assertNull(result)
    }

    @Test
    fun `deleteDelegationByPropertyOwnershipId deletes the delegation`() {
        lettingAgentAccessService.deleteDelegationByPropertyOwnershipId(1L)

        verify(lettingAgentAccessRepository).deleteByPropertyOwnershipId(1L)
    }

    @Test
    fun `addDelegatedPropertyOwnershipToSession adds the id and email to the existing map in the session`() {
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION))
            .thenReturn(mutableMapOf(2L to "other.agent@example.com"))

        lettingAgentAccessService.addDelegatedPropertyOwnershipToSession(1L, "letting.agent@example.com")

        verify(session).setAttribute(
            PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION,
            mapOf(2L to "other.agent@example.com", 1L to "letting.agent@example.com"),
        )
    }

    @Test
    fun `addDelegatedPropertyOwnershipToSession starts a new map when none exists in the session`() {
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION)).thenReturn(null)

        lettingAgentAccessService.addDelegatedPropertyOwnershipToSession(1L, "letting.agent@example.com")

        verify(session).setAttribute(
            PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION,
            mapOf(1L to "letting.agent@example.com"),
        )
    }

    @Test
    fun `getDelegatedPropertyOwnershipEmailsFromSession returns the map from the session`() {
        val delegatedEmails = mutableMapOf(1L to "letting.agent@example.com", 2L to "other.agent@example.com")
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION)).thenReturn(delegatedEmails)

        assertEquals(delegatedEmails, lettingAgentAccessService.getDelegatedPropertyOwnershipEmailsFromSession())
    }

    @Test
    fun `getDelegatedPropertyOwnershipEmailsFromSession returns an empty map when none exists in the session`() {
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION)).thenReturn(null)

        assertEquals(mutableMapOf<Long, String>(), lettingAgentAccessService.getDelegatedPropertyOwnershipEmailsFromSession())
    }
}
