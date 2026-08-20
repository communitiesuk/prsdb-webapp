package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENTS_DELEGATED_THIS_SESSION
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class DelegateToLettingAgentServiceTests {
    @Mock
    private lateinit var mockHttpSession: HttpSession

    @InjectMocks
    private lateinit var delegateToLettingAgentService: DelegateToLettingAgentService

    @Test
    fun `addDelegatedLettingAgentToSession appends the invited email address to the map stored in the session`() {
        val existingDelegations = mutableMapOf((456L to "first@example.com"), (789L to "second@example.com"))
        whenever(mockHttpSession.getAttribute(LETTING_AGENTS_DELEGATED_THIS_SESSION)).thenReturn(existingDelegations)

        delegateToLettingAgentService.addDelegatedLettingAgentToSession(123L, "third@example.com")

        verify(mockHttpSession).setAttribute(
            LETTING_AGENTS_DELEGATED_THIS_SESSION,
            existingDelegations + (123L to "third@example.com"),
        )
    }

    @Test
    fun `addDelegatedLettingAgentToSession stores the invited email address when the session is empty`() {
        whenever(mockHttpSession.getAttribute(LETTING_AGENTS_DELEGATED_THIS_SESSION)).thenReturn(null)

        delegateToLettingAgentService.addDelegatedLettingAgentToSession(123L, "agent@example.com")

        verify(mockHttpSession).setAttribute(
            LETTING_AGENTS_DELEGATED_THIS_SESSION,
            mapOf(123L to "agent@example.com"),
        )
    }

    @Test
    fun `getDelegatedLettingAgentsFromSession returns the map stored in the session`() {
        val delegations = mutableMapOf((456L to "first@example.com"), (789L to "second@example.com"))
        whenever(mockHttpSession.getAttribute(LETTING_AGENTS_DELEGATED_THIS_SESSION)).thenReturn(delegations)

        val result = delegateToLettingAgentService.getDelegatedLettingAgentsFromSession()

        assertEquals(delegations, result)
    }

    @Test
    fun `getDelegatedLettingAgentsFromSession returns an empty map when nothing is stored in the session`() {
        whenever(mockHttpSession.getAttribute(LETTING_AGENTS_DELEGATED_THIS_SESSION)).thenReturn(null)

        val result = delegateToLettingAgentService.getDelegatedLettingAgentsFromSession()

        assertEquals(emptyMap(), result)
    }
}
