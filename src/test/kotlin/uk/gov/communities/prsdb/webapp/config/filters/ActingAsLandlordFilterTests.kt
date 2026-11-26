package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import uk.gov.communities.prsdb.webapp.config.security.ActingAsLandlordAuthenticationToken
import uk.gov.communities.prsdb.webapp.constants.ROLE_ACTING_AS_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService
import uk.gov.communities.prsdb.webapp.services.UserRolesService
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class ActingAsLandlordFilterTests {
    private val actingAsLandlordService: ActingAsLandlordService = mock()
    private val userRolesService: UserRolesService = mock()
    private val filter = ActingAsLandlordFilter(actingAsLandlordService, userRolesService)

    private var chainInvoked = false
    private var contextInChain: SecurityContext? = null
    private var authenticationInChain: Authentication? = null
    private val chain =
        FilterChain { _, _ ->
            chainInvoked = true
            contextInChain = SecurityContextHolder.getContext()
            authenticationInChain = SecurityContextHolder.getContext().authentication
        }

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `doFilter swaps in the landlord identity for a system operator acting as a landlord`() {
        // Arrange
        val systemOperatorToken = createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)
        val systemOperatorContext = SecurityContextImpl(systemOperatorToken)
        SecurityContextHolder.setContext(systemOperatorContext)
        whenever(actingAsLandlordService.getActingAsUserId()).thenReturn(LANDLORD_USER_ID)
        whenever(userRolesService.getLandlordRolesForSubjectId(LANDLORD_USER_ID)).thenReturn(listOf(ROLE_INDIVIDUAL_LANDLORD))

        // Act
        filter.doFilter(MockHttpServletRequest(), MockHttpServletResponse(), chain)

        // Assert
        val actingToken = assertIs<ActingAsLandlordAuthenticationToken>(authenticationInChain)
        assertEquals(LANDLORD_USER_ID, actingToken.name)
        assertEquals(LANDLORD_USER_ID, actingToken.principal)
        assertEquals(SYSTEM_OPERATOR_USER_ID, actingToken.systemOperatorId)
        assertEquals(setOf(ROLE_INDIVIDUAL_LANDLORD, ROLE_ACTING_AS_LANDLORD), actingToken.authorities.map { it.authority }.toSet())
        assertNotSame(systemOperatorContext, contextInChain)
        assertSame(systemOperatorToken, systemOperatorContext.authentication)
    }

    @Test
    fun `doFilter redirects a system operator who is not acting to the system operator dashboard`() {
        // Arrange
        SecurityContextHolder.setContext(SecurityContextImpl(createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)))
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(MockHttpServletRequest(), response, chain)

        // Assert
        assertEquals(SYSTEM_OPERATOR_DASHBOARD_URL, response.redirectedUrl)
        assertFalse(chainInvoked)
        verifyNoInteractions(userRolesService)
    }

    @Test
    fun `doFilter redirects a signed-in user who is not a system operator to the system operator dashboard`() {
        // Arrange
        SecurityContextHolder.setContext(SecurityContextImpl(createOneLoginToken(LANDLORD_USER_ID, ROLE_INDIVIDUAL_LANDLORD)))
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(MockHttpServletRequest(), response, chain)

        // Assert
        assertEquals(SYSTEM_OPERATOR_DASHBOARD_URL, response.redirectedUrl)
        assertFalse(chainInvoked)
        verifyNoInteractions(userRolesService, actingAsLandlordService)
    }

    @Test
    fun `doFilter passes a signed-out request through unchanged`() {
        // Arrange
        val response = MockHttpServletResponse()

        // Act
        filter.doFilter(MockHttpServletRequest(), response, chain)

        // Assert
        assertTrue(chainInvoked)
        assertNull(authenticationInChain)
        assertNull(response.redirectedUrl)
        verifyNoInteractions(userRolesService, actingAsLandlordService)
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
        private const val SYSTEM_OPERATOR_USER_ID = "system-operator-user-id"
        private const val LANDLORD_USER_ID = "landlord-user-id"
    }
}
