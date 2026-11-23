package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_AUTHORISED_ACCESS_TOKENS
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS
import uk.gov.communities.prsdb.webapp.constants.PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class LettingAgentSessionServiceTests {
    @Mock
    private lateinit var session: HttpSession

    @InjectMocks
    private lateinit var lettingAgentSessionService: LettingAgentSessionService

    @Test
    fun `addDelegatedPropertyOwnershipToSession adds the id and email to the existing map in the session`() {
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION))
            .thenReturn(mutableMapOf(2L to "other.agent@example.com"))

        lettingAgentSessionService.addDelegatedPropertyOwnershipToSession(1L, "letting.agent@example.com")

        verify(session).setAttribute(
            PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION,
            mapOf(2L to "other.agent@example.com", 1L to "letting.agent@example.com"),
        )
    }

    @Test
    fun `addDelegatedPropertyOwnershipToSession starts a new map when none exists in the session`() {
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION)).thenReturn(null)

        lettingAgentSessionService.addDelegatedPropertyOwnershipToSession(1L, "letting.agent@example.com")

        verify(session).setAttribute(
            PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION,
            mapOf(1L to "letting.agent@example.com"),
        )
    }

    @Test
    fun `addRemovedLettingAgentToSession stores the email keyed by property ownership id`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS)).thenReturn(null)

        lettingAgentSessionService.addRemovedLettingAgentToSession(1L, "letting.agent@example.com")

        verify(session).setAttribute(
            LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS,
            mapOf(1L to "letting.agent@example.com"),
        )
    }

    @Test
    fun `getDelegatedPropertyOwnershipEmailsFromSession returns the map from the session`() {
        val delegatedEmails = mutableMapOf(1L to "letting.agent@example.com", 2L to "other.agent@example.com")
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION)).thenReturn(delegatedEmails)

        assertEquals(delegatedEmails, lettingAgentSessionService.getDelegatedPropertyOwnershipEmailsFromSession())
    }

    @Test
    fun `getDelegatedPropertyOwnershipEmailsFromSession returns an empty map when none exists in the session`() {
        whenever(session.getAttribute(PROPERTIES_DELEGATED_TO_LETTING_AGENT_THIS_SESSION)).thenReturn(null)

        assertEquals(mutableMapOf<Long, String>(), lettingAgentSessionService.getDelegatedPropertyOwnershipEmailsFromSession())
    }

    @Test
    fun `addRemovedLettingAgentToSession preserves previously removed letting agents`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS))
            .thenReturn(mapOf(1L to "first.agent@example.com"))

        lettingAgentSessionService.addRemovedLettingAgentToSession(2L, "second.agent@example.com")

        verify(session).setAttribute(
            LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS,
            mapOf(1L to "first.agent@example.com", 2L to "second.agent@example.com"),
        )
    }

    @Test
    fun `wasLettingAgentRemovedInThisSession returns true when the property is in the session`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS))
            .thenReturn(mapOf(1L to "letting.agent@example.com"))

        assertTrue(lettingAgentSessionService.wasLettingAgentRemovedInThisSession(1L))
    }

    @Test
    fun `wasLettingAgentRemovedInThisSession returns false when the property is not in the session`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS)).thenReturn(null)

        assertFalse(lettingAgentSessionService.wasLettingAgentRemovedInThisSession(1L))
    }

    @Test
    fun `getRemovedLettingAgentEmailFromSession returns the stored email`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS))
            .thenReturn(mapOf(1L to "letting.agent@example.com"))

        assertEquals("letting.agent@example.com", lettingAgentSessionService.getRemovedLettingAgentEmailFromSession(1L))
    }

    @Test
    fun `getRemovedLettingAgentEmailFromSession returns null when the property is not in the session`() {
        whenever(session.getAttribute(LETTING_AGENTS_REMOVED_THIS_SESSION_WITH_EMAILS)).thenReturn(null)

        assertNull(lettingAgentSessionService.getRemovedLettingAgentEmailFromSession(1L))
    }

    @Nested
    inner class AddJourneyIdInvitationTokenPairToSession {
        @Test
        fun `addJourneyIdInvitationTokenPairToSession adds pair to empty session`() {
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(null)

            lettingAgentSessionService.addJourneyIdInvitationTokenPairToSession("journey1", "token1")

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

            lettingAgentSessionService.addJourneyIdInvitationTokenPairToSession("journey2", "token2")

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

            assertEquals("token2", lettingAgentSessionService.getInvitationTokenForJourneyIdFromSession("journey2"))
        }

        @Test
        fun `getInvitationTokenForJourneyIdFromSession throws when journey id does not exist`() {
            val pairs = mutableListOf(Pair("journey1", "token1"))
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(pairs)

            assertThrows<PrsdbWebException> {
                lettingAgentSessionService.getInvitationTokenForJourneyIdFromSession("nonexistent")
            }
        }

        @Test
        fun `getInvitationTokenForJourneyIdFromSessionOrNull returns null when journey id does not exist`() {
            val pairs = mutableListOf(Pair("journey1", "token1"))
            whenever(session.getAttribute(LETTING_AGENT_INVITATION_TOKEN_WITH_JOURNEY_IDS))
                .thenReturn(pairs)

            assertNull(lettingAgentSessionService.getInvitationTokenForJourneyIdFromSessionOrNull("nonexistent"))
        }

        @Test
        fun `addAuthorisedTokenToSession adds the token to an empty session set`() {
            whenever(session.getAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS)).thenReturn(null)
            val token = UUID.randomUUID().toString()

            lettingAgentSessionService.addAuthorisedTokenToSession(token)

            verify(session).setAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS, mutableSetOf(token))
        }

        @Test
        fun `addAuthorisedTokenToSession preserves previously authorised tokens`() {
            val existing = UUID.randomUUID().toString()
            whenever(session.getAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS)).thenReturn(mutableSetOf(existing))
            val token = UUID.randomUUID().toString()

            lettingAgentSessionService.addAuthorisedTokenToSession(token)

            verify(session).setAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS, mutableSetOf(existing, token))
        }

        @Test
        fun `isTokenAuthorisedInSession returns true when the token is present`() {
            val token = UUID.randomUUID().toString()
            whenever(session.getAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS)).thenReturn(mutableSetOf(token))

            assertTrue(lettingAgentSessionService.isTokenAuthorisedInSession(token))
        }

        @Test
        fun `isTokenAuthorisedInSession returns false when the token is absent`() {
            whenever(session.getAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS)).thenReturn(null)

            assertFalse(lettingAgentSessionService.isTokenAuthorisedInSession(UUID.randomUUID().toString()))
        }

        @Test
        fun `removeAuthorisedTokenFromSession removes the token from the session set`() {
            val token = UUID.randomUUID().toString()
            whenever(session.getAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS))
                .thenReturn(mutableSetOf(token, "other"))

            lettingAgentSessionService.removeAuthorisedTokenFromSession(token)

            verify(session).setAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS, mutableSetOf("other"))
        }

        @Test
        fun `removeAuthorisedTokenFromSession does not write to the session when the token is absent`() {
            whenever(session.getAttribute(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS)).thenReturn(mutableSetOf("other"))

            lettingAgentSessionService.removeAuthorisedTokenFromSession(UUID.randomUUID().toString())

            verify(session, never()).setAttribute(eq(LETTING_AGENT_AUTHORISED_ACCESS_TOKENS), any())
        }
    }
}
