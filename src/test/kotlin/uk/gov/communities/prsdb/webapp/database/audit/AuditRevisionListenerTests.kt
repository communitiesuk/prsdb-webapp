package uk.gov.communities.prsdb.webapp.database.audit

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.config.interceptors.LettingAgentAccessInterceptor
import uk.gov.communities.prsdb.webapp.database.entity.AuditRevision

class AuditRevisionListenerTests {
    private val listener = AuditRevisionListener()

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
        RequestContextHolder.resetRequestAttributes()
    }

    @Test
    fun `newRevision records the subject identifier of an authenticated user`() {
        SecurityContextHolder.getContext().authentication =
            TestingAuthenticationToken("urn:fdc:gov.uk:2022:USER", null, "ROLE_LANDLORD")
        val revision = AuditRevision()

        listener.newRevision(revision)

        assertEquals("urn:fdc:gov.uk:2022:USER", revision.userSubjectIdentifier)
        assertNull(revision.lettingAgentAccessId)
    }

    @Test
    fun `newRevision records no user for an anonymous authentication`() {
        SecurityContextHolder.getContext().authentication =
            AnonymousAuthenticationToken("key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"))
        val revision = AuditRevision()

        listener.newRevision(revision)

        assertNull(revision.userSubjectIdentifier)
    }

    @Test
    fun `newRevision records the letting agent access id held on the current request`() {
        val request = MockHttpServletRequest()
        request.setAttribute(LettingAgentAccessInterceptor.LETTING_AGENT_ACCESS_ID_ATTRIBUTE, 42L)
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        val revision = AuditRevision()

        listener.newRevision(revision)

        assertEquals(42L, revision.lettingAgentAccessId)
        assertNull(revision.userSubjectIdentifier)
    }

    @Test
    fun `newRevision records neither a user nor a letting agent outside a request`() {
        val revision = AuditRevision()

        listener.newRevision(revision)

        assertNull(revision.userSubjectIdentifier)
        assertNull(revision.lettingAgentAccessId)
    }
}
