package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import uk.gov.communities.prsdb.webapp.config.security.ActingAsLandlordAuthenticationToken
import uk.gov.communities.prsdb.webapp.constants.ACTING_USER_ID
import uk.gov.communities.prsdb.webapp.constants.ROLE_ACTING_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ActingAsLandlordFilterTests {
    private val userRolesService: UserRolesService = mock()
    private val filter = ActingAsLandlordFilter(userRolesService)

    private var chainInvoked = false
    private var authenticationInChain: Authentication? = null
    private val chain =
        FilterChain { _, _ ->
            chainInvoked = true
            authenticationInChain = SecurityContextHolder.getContext().authentication
        }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `doFilter swaps in the landlord identity for an operator with a stored acting user ID`() {
        // Arrange
        val operatorToken = createOneLoginToken(OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)
        val operatorContext = SecurityContextImpl(operatorToken)
        SecurityContextHolder.setContext(operatorContext)
        whenever(userRolesService.getLandlordRolesForSubjectId(LANDLORD_USER_ID)).thenReturn(listOf(ROLE_INDIVIDUAL_LANDLORD))

        // Act
        filter.doFilter(createRequest(actingUserId = LANDLORD_USER_ID), MockHttpServletResponse(), chain)

        // Assert
        val actingToken = assertIs<ActingAsLandlordAuthenticationToken>(authenticationInChain)
        assertEquals(LANDLORD_USER_ID, actingToken.name)
        assertEquals(LANDLORD_USER_ID, actingToken.principal)
        assertEquals(OPERATOR_USER_ID, actingToken.operatorId)
        assertEquals(setOf(ROLE_INDIVIDUAL_LANDLORD, ROLE_ACTING_AS_LANDLORD), actingToken.authorities.map { it.authority }.toSet())
        assertSame(operatorToken, operatorContext.authentication)
    }

    @Test
    fun `doFilter redirects an operator without a stored acting user ID to the operator dashboard`() {
        // Arrange
        SecurityContextHolder.setContext(SecurityContextImpl(createOneLoginToken(OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)))
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(createRequest(actingUserId = null), response, chain)

        // Assert
        assertEquals(SYSTEM_OPERATOR_DASHBOARD_URL, response.redirectedUrl)
        assertFalse(chainInvoked)
        verifyNoInteractions(userRolesService)
    }

    @Test
    fun `doFilter redirects a signed-in user who is not an operator to the operator dashboard`() {
        // Arrange
        SecurityContextHolder.setContext(SecurityContextImpl(createOneLoginToken(LANDLORD_USER_ID, ROLE_INDIVIDUAL_LANDLORD)))
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(createRequest(actingUserId = OTHER_LANDLORD_USER_ID), response, chain)

        // Assert
        assertEquals(SYSTEM_OPERATOR_DASHBOARD_URL, response.redirectedUrl)
        assertFalse(chainInvoked)
        verifyNoInteractions(userRolesService)
    }

    @Test
    fun `doFilter passes an unauthenticated request through unchanged`() {
        // Arrange
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(createRequest(actingUserId = LANDLORD_USER_ID), response, chain)

        // Assert
        assertTrue(chainInvoked)
        assertNull(authenticationInChain)
        assertNull(response.redirectedUrl)
        verifyNoInteractions(userRolesService)
    }

    private fun createRequest(actingUserId: String?): MockHttpServletRequest {
        val session = MockHttpSession()
        actingUserId?.let { session.setAttribute(ACTING_USER_ID, it) }
        return MockHttpServletRequest().apply { setSession(session) }
    }

    private fun createOneLoginToken(
        subject: String,
        role: String,
    ): OAuth2AuthenticationToken {
        val authorities = listOf(SimpleGrantedAuthority(role))
        val idToken =
            OidcIdToken
                .withTokenValue("id-token")
                .subject(subject)
                .issuer("http://localhost")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build()
        return OAuth2AuthenticationToken(DefaultOidcUser(authorities, idToken), authorities, "one-login")
    }

    companion object {
        private const val OPERATOR_USER_ID = "operator-user-id"
        private const val LANDLORD_USER_ID = "landlord-user-id"
        private const val OTHER_LANDLORD_USER_ID = "other-landlord-user-id"
    }
}
