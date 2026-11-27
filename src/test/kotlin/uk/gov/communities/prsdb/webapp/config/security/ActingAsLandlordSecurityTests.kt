package uk.gov.communities.prsdb.webapp.config.security

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.data.domain.PageImpl
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
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTING_AS_USER_ID
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_PHASE_THREE
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.ACTING_AS_LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.OUTSTANDING_ACTIONS_URL
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.PropertyComplianceService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.services.UsersIncompletePropertyService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData.Companion.createIndividualLandlord
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertSame

@WebMvcTest(LandlordController::class)
class ActingAsLandlordSecurityTests(
    @Autowired val webContext: WebApplicationContext,
) : ControllerTest(webContext) {
    @Autowired
    private lateinit var securityContextRepository: SecurityContextRepository

    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var usersIncompletePropertyService: UsersIncompletePropertyService

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    private var authenticationSeenByHandler: Authentication? = null

    @BeforeEach
    fun captureAuthenticationSeenByHandler() {
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenAnswer {
            authenticationSeenByHandler = SecurityContextHolder.getContext().authentication
            createIndividualLandlord()
        }
    }

    @Test
    fun `an acting request reaches the dashboard as the landlord and leaves the system operator login in the session`() {
        // Arrange
        val systemOperatorToken = createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)
        val systemOperatorContext = SecurityContextImpl(systemOperatorToken)
        val session = createSession(systemOperatorContext, actingAsUserId = LANDLORD_USER_ID)
        whenever(userRolesService.getLandlordRolesForSubjectId(LANDLORD_USER_ID)).thenReturn(listOf(ROLE_INDIVIDUAL_LANDLORD))

        // Act
        mvc.get(ACTING_AS_LANDLORD_DASHBOARD_URL) { this.session = session }.andExpect {
            status { isOk() }
            view { name("landlordDashboard") }
        }

        // Assert
        val actingToken = assertIs<ActingAsLandlordAuthenticationToken>(authenticationSeenByHandler)
        assertEquals(LANDLORD_USER_ID, actingToken.name)
        assertEquals(SYSTEM_OPERATOR_USER_ID, actingToken.systemOperatorId)
        assertSame(systemOperatorContext, session.getAttribute(SPRING_SECURITY_CONTEXT_KEY))
        assertSame(systemOperatorToken, systemOperatorContext.authentication)
    }

    @Test
    fun `an acting system operator sees the landlord's outstanding actions`() {
        // Arrange
        val session =
            createSession(
                SecurityContextImpl(createOneLoginToken(SYSTEM_OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)),
                actingAsUserId = LANDLORD_USER_ID,
            )
        whenever(userRolesService.getLandlordRolesForSubjectId(LANDLORD_USER_ID)).thenReturn(listOf(ROLE_INDIVIDUAL_LANDLORD))
        whenever(featureFlagManager.checkFeature(PROPERTY_REGISTRATION_PHASE_THREE)).thenReturn(true)
        whenever(propertyOwnershipService.getOutstandingActionsForLandlord(any(), any())).thenReturn(PageImpl(emptyList()))

        // Act
        mvc.get("/$SYSTEM_OPERATOR_PATH_SEGMENT$OUTSTANDING_ACTIONS_URL") { this.session = session }.andExpect {
            status { isOk() }
            view { name("outstandingActions") }
        }

        // Assert
        val actingToken = assertIs<ActingAsLandlordAuthenticationToken>(authenticationSeenByHandler)
        assertEquals(LANDLORD_USER_ID, actingToken.name)
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
        mvc.get(ACTING_AS_LANDLORD_DASHBOARD_URL) { this.session = session }.andExpect {
            status { is3xxRedirection() }
            redirectedUrl(SYSTEM_OPERATOR_DASHBOARD_URL)
        }
    }

    @Test
    fun `a signed out request is redirected to One Login`() {
        mvc.get(ACTING_AS_LANDLORD_DASHBOARD_URL).andExpect {
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

    companion object {
        private const val SYSTEM_OPERATOR_USER_ID = "system-operator-user-id"
        private const val LANDLORD_USER_ID = "landlord-user-id"
    }
}
