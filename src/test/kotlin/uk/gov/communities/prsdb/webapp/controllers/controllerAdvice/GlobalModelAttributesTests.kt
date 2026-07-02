package uk.gov.communities.prsdb.webapp.controllers.controllerAdvice

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.ui.ExtendedModelMap
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.models.viewModels.NavigationLinkViewModel
import uk.gov.communities.prsdb.webapp.services.BackUrlStorageService
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class GlobalModelAttributesTests {
    @Mock
    private lateinit var backUrlStorageService: BackUrlStorageService

    @Mock
    private lateinit var messageSource: MessageSource

    private val defaultServiceName = "Register your rental property"
    private val customServiceName = "Check a rental property or landlord"

    private fun createGlobalModelAttributes(): GlobalModelAttributes {
        val globalModelAttributes = GlobalModelAttributes(backUrlStorageService, messageSource)
        ReflectionTestUtils.setField(globalModelAttributes, "plausibleSiteId", "test-site-id")
        return globalModelAttributes
    }

    @Test
    fun `addGlobalModelAttributes sets serviceName to custom name for local council routes`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/$LOCAL_COUNCIL_PATH_SEGMENT/start"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(customServiceName, model["serviceName"])
        assertTrue(model["isCustomServiceName"] as Boolean)
    }

    @Test
    fun `addGlobalModelAttributes sets serviceName to custom name for system operator routes`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/$SYSTEM_OPERATOR_PATH_SEGMENT/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(customServiceName, model["serviceName"])
        assertTrue(model["isCustomServiceName"] as Boolean)
    }

    @Test
    fun `addGlobalModelAttributes sets serviceName to default name for other routes`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(defaultServiceName, model["serviceName"])
        assertNull(model["isCustomServiceName"])
    }

    @Test
    fun `addGlobalModelAttributes sets privacyUrl with a backUrl query param so the privacy page renders a back link`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        whenever(backUrlStorageService.storeCurrentUrlReturningKey()).thenReturn(42)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals("/privacy-notice?withBackUrl=42", model["privacyUrl"])
    }

    @Test
    fun `addGlobalModelAttributes sets showOneLoginNav to true for one-login users`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/dashboard"
        request.userPrincipal = createOAuth2AuthenticationToken("one-login")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(true, model["showOneLoginNav"])
    }

    @Test
    fun `addGlobalModelAttributes sets showOneLoginNav to false for internal-access users`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/cookies"
        request.userPrincipal = createOAuth2AuthenticationToken("internal-access")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(false, model["showOneLoginNav"])
    }

    @Test
    fun `addGlobalModelAttributes sets showOneLoginNav to false for unauthenticated users`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/register-as-a-landlord"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertEquals(false, model["showOneLoginNav"])
    }

    @Test
    fun `addGlobalModelAttributes adds a landlord dashboard nav link on landlord pages for landlords`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/dashboard"
        request.addUserRole("LANDLORD")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        @Suppress("UNCHECKED_CAST")
        val navLinks = model["navLinks"] as List<NavigationLinkViewModel>
        assertEquals(1, navLinks.size)
        assertEquals("/landlord/dashboard", navLinks[0].href)
        assertEquals("navLink.dashboard.title", navLinks[0].messageProperty)
        assertTrue(navLinks[0].isActive)
    }

    @Test
    fun `addGlobalModelAttributes marks the landlord dashboard link inactive on other landlord pages`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/incomplete-properties"
        request.addUserRole("LANDLORD")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        @Suppress("UNCHECKED_CAST")
        val navLinks = model["navLinks"] as List<NavigationLinkViewModel>
        assertEquals(1, navLinks.size)
        assertEquals("/landlord/dashboard", navLinks[0].href)
        assertFalse(navLinks[0].isActive)
    }

    @Test
    fun `addGlobalModelAttributes adds no nav link on landlord pages for a user without the landlord role`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord/register-as-a-landlord"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertNull(model["navLinks"])
    }

    @Test
    fun `addGlobalModelAttributes does not treat landlord-details as a landlord service page`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/landlord-details"
        request.addUserRole("LANDLORD")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertNull(model["navLinks"])
    }

    @Test
    fun `addGlobalModelAttributes adds a local council dashboard nav link on LC pages for LC users`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/local-council/dashboard"
        request.addUserRole("LOCAL_COUNCIL_USER")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        @Suppress("UNCHECKED_CAST")
        val navLinks = model["navLinks"] as List<NavigationLinkViewModel>
        assertEquals(1, navLinks.size)
        assertEquals("/local-council/dashboard", navLinks[0].href)
        assertEquals("navLink.dashboard.title", navLinks[0].messageProperty)
        assertTrue(navLinks[0].isActive)
    }

    @Test
    fun `addGlobalModelAttributes adds a local council dashboard nav link on LC pages for LC admins`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/local-council/1/manage-users"
        request.addUserRole("LOCAL_COUNCIL_ADMIN")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        @Suppress("UNCHECKED_CAST")
        val navLinks = model["navLinks"] as List<NavigationLinkViewModel>
        assertEquals(1, navLinks.size)
        assertEquals("/local-council/dashboard", navLinks[0].href)
        assertFalse(navLinks[0].isActive)
    }

    @Test
    fun `addGlobalModelAttributes adds no nav link on LC pages for a user without an LC role`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/local-council/register-local-council-user"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertNull(model["navLinks"])
    }

    @Test
    fun `addGlobalModelAttributes adds a system operator dashboard nav link on system operator pages`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/system-operator/dashboard"
        request.addUserRole("SYSTEM_OPERATOR")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        @Suppress("UNCHECKED_CAST")
        val navLinks = model["navLinks"] as List<NavigationLinkViewModel>
        assertEquals(1, navLinks.size)
        assertEquals("/system-operator/dashboard", navLinks[0].href)
        assertEquals("navLink.dashboard.title", navLinks[0].messageProperty)
        assertTrue(navLinks[0].isActive)
    }

    @Test
    fun `addGlobalModelAttributes adds no nav link on system operator pages without the system operator role`() {
        whenever(messageSource.getMessage(eq("localCouncilServiceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(customServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/system-operator/dashboard"

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertNull(model["navLinks"])
    }

    @Test
    fun `addGlobalModelAttributes adds no nav link on non-service pages`() {
        whenever(messageSource.getMessage(eq("serviceName"), anyOrNull(), any<String>(), any()))
            .thenReturn(defaultServiceName)
        val globalModelAttributes = createGlobalModelAttributes()
        val model = ExtendedModelMap()
        val request = MockHttpServletRequest()
        request.requestURI = "/cookies"
        request.addUserRole("LANDLORD")

        globalModelAttributes.addGlobalModelAttributes(model, request)

        assertNull(model["navLinks"])
    }

    private fun createOAuth2AuthenticationToken(registrationId: String): OAuth2AuthenticationToken {
        val idToken =
            OidcIdToken.withTokenValue("mock-token")
                .subject("mock-user")
                .issuer("http://localhost")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build()
        val oidcUser = DefaultOidcUser(emptyList(), idToken)
        return OAuth2AuthenticationToken(oidcUser, emptyList(), registrationId)
    }
}
