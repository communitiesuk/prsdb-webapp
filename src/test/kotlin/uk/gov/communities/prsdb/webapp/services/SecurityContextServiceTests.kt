package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SecurityContextServiceTests {
    private val securityContextRepository = HttpSessionSecurityContextRepository()
    private val securityContextService = SecurityContextService(securityContextRepository)

    private lateinit var request: MockHttpServletRequest
    private lateinit var response: MockHttpServletResponse
    private val authentication = TestingAuthenticationToken("system-operator", null, "ROLE_SYSTEM_OPERATOR")

    @BeforeEach
    fun setUp() {
        request = MockHttpServletRequest()
        response = MockHttpServletResponse()
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request, response))

        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = authentication
        SecurityContextHolder.setContext(context)
    }

    @AfterEach
    fun tearDown() {
        RequestContextHolder.resetRequestAttributes()
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `resetSession invalidates the old session and creates a new one with a different ID`() {
        // Arrange
        val oldSession = request.getSession(true) as MockHttpSession
        val oldSessionId = oldSession.id

        // Act
        securityContextService.resetSession()

        // Assert
        assertTrue(oldSession.isInvalid)
        val newSession = assertNotNull(request.getSession(false))
        assertNotEquals(oldSessionId, newSession.id)
    }

    @Test
    fun `resetSession saves the current security context into the new session`() {
        // Arrange
        request.getSession(true)

        // Act
        securityContextService.resetSession()

        // Assert
        val savedContext = securityContextRepository.loadDeferredContext(request).get()
        assertEquals(authentication, savedContext.authentication)
    }

    @Test
    fun `resetSession does not carry other attributes over to the new session`() {
        // Arrange
        request.getSession(true)!!.setAttribute("journeyState", "some state")

        // Act
        securityContextService.resetSession()

        // Assert
        assertNull(request.getSession(false)!!.getAttribute("journeyState"))
    }

    @Test
    fun `resetSession means a CSRF token saved before the reset can no longer be loaded`() {
        // Arrange
        val csrfTokenRepository = HttpSessionCsrfTokenRepository()
        csrfTokenRepository.saveToken(csrfTokenRepository.generateToken(request), request, response)

        // Act
        securityContextService.resetSession()

        // Assert
        assertNull(csrfTokenRepository.loadToken(request))
    }

    @Test
    fun `resetSession creates a session holding the security context when there was no existing session`() {
        // Act
        securityContextService.resetSession()

        // Assert
        assertNotNull(request.getSession(false))
        val savedContext = securityContextRepository.loadDeferredContext(request).get()
        assertEquals(authentication, savedContext.authentication)
    }
}
