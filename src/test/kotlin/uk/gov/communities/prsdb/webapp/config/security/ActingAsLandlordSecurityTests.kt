package uk.gov.communities.prsdb.webapp.config.security

import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.get
import org.springframework.web.context.WebApplicationContext
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTING_USER_ID
import uk.gov.communities.prsdb.webapp.constants.ROLE_INDIVIDUAL_LANDLORD
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.ControllerTest
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService
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
    @MockitoBean
    private lateinit var userToLandlordService: UserToLandlordService

    @MockitoBean
    private lateinit var localCouncilService: LocalCouncilService

    @MockitoBean
    private lateinit var propertyOwnershipService: PropertyOwnershipService

    @MockitoBean
    private lateinit var propertyComplianceService: PropertyComplianceService

    @MockitoBean
    private lateinit var usersIncompletePropertyService: UsersIncompletePropertyService

    @MockitoBean
    private lateinit var featureFlagManager: FeatureFlagManager

    @Autowired
    private lateinit var securityContextRepository: SecurityContextRepository

    @Test
    fun `an acting request reaches the handler as the landlord and leaves the operator login stored in the session`() {
        // Arrange
        val operatorToken = createOneLoginToken(OPERATOR_USER_ID, ROLE_SYSTEM_OPERATOR)
        val operatorContext = SecurityContextImpl(operatorToken)
        val operatorSession = MockHttpSession()
        operatorSession.setAttribute(SPRING_SECURITY_CONTEXT_KEY, operatorContext)
        operatorSession.setAttribute(ACTING_USER_ID, LANDLORD_USER_ID)
        whenever(userRolesService.getLandlordRolesForSubjectId(LANDLORD_USER_ID)).thenReturn(listOf(ROLE_INDIVIDUAL_LANDLORD))
        var authenticationSeenByHandler: Authentication? = null
        whenever(userToLandlordService.getCurrentLandlordForUser()).thenAnswer {
            authenticationSeenByHandler = SecurityContextHolder.getContext().authentication
            createIndividualLandlord()
        }

        // Act
        mvc
            .get("/$SYSTEM_OPERATOR_PATH_SEGMENT$LANDLORD_DASHBOARD_URL") { session = operatorSession }
            .andExpect { status { isOk() } }

        // Assert
        val actingToken = assertIs<ActingAsLandlordAuthenticationToken>(authenticationSeenByHandler)
        assertEquals(LANDLORD_USER_ID, actingToken.name)
        assertEquals(OPERATOR_USER_ID, actingToken.operatorId)
        assertSame(operatorContext, operatorSession.getAttribute(SPRING_SECURITY_CONTEXT_KEY))
        assertSame(operatorToken, operatorContext.authentication)
    }

    @Test
    fun `the security context repository refuses to save an acting token`() {
        // Arrange
        val actingContext =
            SecurityContextImpl(
                ActingAsLandlordAuthenticationToken(LANDLORD_USER_ID, listOf(ROLE_INDIVIDUAL_LANDLORD), OPERATOR_USER_ID),
            )

        // Act, Assert
        assertFailsWith<IllegalStateException> {
            securityContextRepository.saveContext(actingContext, MockHttpServletRequest(), MockHttpServletResponse())
        }
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
    }
}
