package uk.gov.communities.prsdb.webapp.config.security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import uk.gov.communities.prsdb.webapp.constants.ACTING_AS_USER_ID
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

// TODO PDJB-1911: Test against the real acting landlord dashboard route instead of this test-only controller
@WebMvcTest(ActingAsLandlordSecurityTests.ActingAsLandlordTestController::class)
@Import(ActingAsLandlordSecurityTests.ActingAsLandlordTestController::class)
class ActingAsLandlordSecurityTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Autowired
    private lateinit var securityContextRepository: SecurityContextRepository

    @Autowired
    private lateinit var testController: ActingAsLandlordTestController

    @Test
    fun `an acting request reaches the handler as the landlord and leaves the system operator login in the session`() {
        // Arrange
        val systemOperatorToken = createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)
        val systemOperatorContext = SecurityContextImpl(systemOperatorToken)
        val session = createSession(systemOperatorContext, actingAsUserId = LANDLORD_USER_ID)
        whenever(userRolesService.getLandlordRolesForSubjectId(LANDLORD_USER_ID)).thenReturn(listOf(ROLE_INDIVIDUAL_LANDLORD))

        // Act
        mvc.get(TEST_ROUTE) { this.session = session }.andExpect { status { isOk() } }

        // Assert
        val actingToken = assertIs<ActingAsLandlordAuthenticationToken>(testController.authenticationSeenByHandler)
        assertEquals(LANDLORD_USER_ID, actingToken.name)
        assertEquals(SYSTEM_OPERATOR_USER_ID, actingToken.systemOperatorId)
        assertSame(systemOperatorContext, session.getAttribute(SPRING_SECURITY_CONTEXT_KEY))
        assertSame(systemOperatorToken, systemOperatorContext.authentication)
    }

    @Test
    fun `a system operator who is not acting is redirected to the system operator dashboard`() {
        // Arrange
        val session =
            createSession(
                SecurityContextImpl(createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)),
                actingAsUserId = null,
            )

        // Act, Assert
        mvc.get(TEST_ROUTE) { this.session = session }.andExpect {
            status { is3xxRedirection() }
            redirectedUrl(SYSTEM_OPERATOR_DASHBOARD_URL)
        }
    }

    @Test
    fun `a signed out request is redirected to One Login`() {
        mvc.get(TEST_ROUTE).andExpect {
            status { is3xxRedirection() }
            redirectedUrl("/oauth2/authorization/one-login")
        }
    }

    @Test
    fun `the security context repository bean refuses to save an acting token`() {
        // Arrange
        val actingContext =
            SecurityContextImpl(
                ActingAsLandlordAuthenticationToken(LANDLORD_USER_ID, listOf(ROLE_INDIVIDUAL_LANDLORD), SYSTEM_OPERATOR_USER_ID),
            )

        // Act, Assert
        assertFailsWith<IllegalStateException> {
            securityContextRepository.saveContext(actingContext, MockHttpServletRequest(), MockHttpServletResponse())
        }
    }

    // TODO PDJB-1907: Add an integration test that starts acting and follows a /landlord/** link into the acting area
    @Test
    fun `a landlord route requested while acting is redirected to the acting route with its query string`() {
        // Arrange
        val session =
            createSession(
                SecurityContextImpl(createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)),
                actingAsUserId = LANDLORD_USER_ID,
            )

        // Act, Assert
        mvc.get("$LANDLORD_ROUTE?$QUERY_STRING") { this.session = session }.andExpect {
            status { isTemporaryRedirect() }
            header { string(HttpHeaders.LOCATION, "/$SYSTEM_OPERATOR_PATH_SEGMENT$LANDLORD_ROUTE?$QUERY_STRING") }
        }
    }

    @Test
    fun `a landlord route requested while not acting is not redirected to the acting route`() {
        // Arrange
        val session =
            createSession(
                SecurityContextImpl(createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)),
                actingAsUserId = null,
            )

        // Act, Assert
        mvc.get(LANDLORD_ROUTE) { this.session = session }.andExpect {
            header { doesNotExist(HttpHeaders.LOCATION) }
        }
    }

    private fun createSession(
        context: SecurityContext,
        actingAsUserId: String?,
    ) = MockHttpSession().apply {
        setAttribute(SPRING_SECURITY_CONTEXT_KEY, context)
        actingAsUserId?.let { setAttribute(ACTING_AS_USER_ID, it) }
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

    @PrsdbRestController
    @RequestMapping(TEST_ROUTE)
    class ActingAsLandlordTestController {
        var authenticationSeenByHandler: Authentication? = null

        @GetMapping
        fun get(): String {
            authenticationSeenByHandler = SecurityContextHolder.getContext().authentication
            return "ok"
        }
    }

    companion object {
        private const val TEST_ROUTE = "/$SYSTEM_OPERATOR_PATH_SEGMENT/$LANDLORD_PATH_SEGMENT/acting-test"
        private const val LANDLORD_ROUTE = "/$LANDLORD_PATH_SEGMENT/dashboard"
        private const val QUERY_STRING = "foo=bar"
        private const val SYSTEM_OPERATOR_USER_ID = "system-operator-user-id"
        private const val LANDLORD_USER_ID = "landlord-user-id"
    }
}
