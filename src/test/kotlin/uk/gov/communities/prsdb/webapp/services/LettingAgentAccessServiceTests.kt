package uk.gov.communities.prsdb.webapp.services

import jakarta.persistence.EntityNotFoundException
import jakarta.servlet.http.HttpSession
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.database.entity.LettingAgentAccess
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
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
    fun `propertyHasLettingAgent returns true when a delegation exists and the property is occupied`() {
        val propertyOwnership = MockLandlordData.createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnership.id))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(propertyOwnership = propertyOwnership))

        assertTrue(lettingAgentAccessService.propertyHasLettingAgent(propertyOwnership))
    }

    @Test
    fun `propertyHasLettingAgent returns false when no delegation exists`() {
        val propertyOwnership = MockLandlordData.createOccupiedPropertyOwnership()
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnership.id)).thenReturn(null)

        assertFalse(lettingAgentAccessService.propertyHasLettingAgent(propertyOwnership))
    }

    @Test
    fun `propertyHasLettingAgent returns false when the property is not occupied`() {
        val propertyOwnership = MockLandlordData.createUnoccupiedPropertyOwnership()
        whenever(lettingAgentAccessRepository.findByPropertyOwnershipId(propertyOwnership.id))
            .thenReturn(MockLettingAgentData.createLettingAgentAccess(propertyOwnership = propertyOwnership))

        assertFalse(lettingAgentAccessService.propertyHasLettingAgent(propertyOwnership))
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
    fun `addRemovedLettingAgentToSession stores the email keyed by property ownership id`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS)).thenReturn(null)

        lettingAgentAccessService.addRemovedLettingAgentToSession(1L, "letting.agent@example.com")

        verify(session).setAttribute(
            LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS,
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

    @Test
    fun `addRemovedLettingAgentToSession preserves previously removed letting agents`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS))
            .thenReturn(mapOf(1L to "first.agent@example.com"))

        lettingAgentAccessService.addRemovedLettingAgentToSession(2L, "second.agent@example.com")

        verify(session).setAttribute(
            LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS,
            mapOf(1L to "first.agent@example.com", 2L to "second.agent@example.com"),
        )
    }

    @Test
    fun `wasLettingAgentRemovedInThisSession returns true when the property is in the session`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS))
            .thenReturn(mapOf(1L to "letting.agent@example.com"))

        assertTrue(lettingAgentAccessService.wasLettingAgentRemovedInThisSession(1L))
    }

    @Test
    fun `wasLettingAgentRemovedInThisSession returns false when the property is not in the session`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS)).thenReturn(null)

        assertFalse(lettingAgentAccessService.wasLettingAgentRemovedInThisSession(1L))
    }

    @Test
    fun `getRemovedLettingAgentEmailFromSession returns the stored email`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS))
            .thenReturn(mapOf(1L to "letting.agent@example.com"))

        assertEquals("letting.agent@example.com", lettingAgentAccessService.getRemovedLettingAgentEmailFromSession(1L))
    }

    @Test
    fun `getRemovedLettingAgentEmailFromSession returns null when the property is not in the session`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS)).thenReturn(null)

        assertNull(lettingAgentAccessService.getRemovedLettingAgentEmailFromSession(1L))
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
    inner class AddJourneyIdInvitationTokenPairToSession {
        @Test
        fun `addJourneyIdInvitationTokenPairToSession adds pair to empty session`() {
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(null)

            lettingAgentAccessService.addJourneyIdInvitationTokenPairToSession("journey1", "token1")

            val captor = argumentCaptor<MutableList<Pair<String, String>>>()
            verify(session).setAttribute(
                eq(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey1", "token1")), captor.firstValue)
        }

        @Test
        fun `addJourneyIdInvitationTokenPairToSession appends pair to existing pairs`() {
            val existingPairs = mutableListOf(Pair("journey1", "token1"))
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(existingPairs)

            lettingAgentAccessService.addJourneyIdInvitationTokenPairToSession("journey2", "token2")

            val captor = argumentCaptor<MutableList<Pair<String, String>>>()
            verify(session).setAttribute(
                eq(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS),
                captor.capture(),
            )
            assertEquals(listOf(Pair("journey1", "token1"), Pair("journey2", "token2")), captor.firstValue)
        }
    }

    @Nested
    inner class GetInvitationTokenForJourneyIdFromSession {
        @Test
        fun `getInvitationTokenForJourneyIdFromSession returns token when journey id exists`() {
            val pairs = mutableListOf(Pair("journey1", "token1"), Pair("journey2", "token2"))
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(pairs)

            assertEquals("token2", lettingAgentAccessService.getInvitationTokenForJourneyIdFromSession("journey2"))
        }

        @Test
        fun `getInvitationTokenForJourneyIdFromSession throws when journey id does not exist`() {
            val pairs = mutableListOf(Pair("journey1", "token1"))
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(pairs)

            assertThrows<PrsdbWebException> {
                lettingAgentAccessService.getInvitationTokenForJourneyIdFromSession("nonexistent")
            }
        }

        @Test
        fun `getInvitationTokenForJourneyIdFromSession throws when session attribute is null`() {
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(null)

            assertThrows<PrsdbWebException> {
                lettingAgentAccessService.getInvitationTokenForJourneyIdFromSession("journey1")
            }
        }
    }
}
