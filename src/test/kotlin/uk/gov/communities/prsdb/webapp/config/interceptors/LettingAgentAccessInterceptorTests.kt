package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.mock.web.MockHttpServletRequest
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVALID_LINK_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class LettingAgentAccessInterceptorTests {
    private val mockRequest = MockHttpServletRequest()

    @Mock
    private lateinit var mockResponse: HttpServletResponse

    @Mock
    private lateinit var mockLettingAgentAccessService: LettingAgentAccessService

    @InjectMocks
    private lateinit var interceptor: LettingAgentAccessInterceptor

    private val token = UUID.randomUUID()
    private val propertyDetailsUri = LettingAgentPropertyDetailsController.getLettingAgentPropertyDetailsPath(token)

    private fun callPreHandle() = interceptor.preHandle(mockRequest, mockResponse, handler = Any())

    @Test
    fun `preHandle allows a request to property details with a valid token that is authorised in the session`() {
        mockRequest.requestURI = propertyDetailsUri
        whenever(mockLettingAgentAccessService.getTokenIsValid(token.toString())).thenReturn(true)
        whenever(mockLettingAgentAccessService.isTokenAuthorisedInSession(token.toString())).thenReturn(true)

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle allows a valid authorised token for a property-details update sub-route`() {
        mockRequest.requestURI = "$propertyDetailsUri/update-rent-includes-bills/check-your-answers"
        whenever(mockLettingAgentAccessService.getTokenIsValid(token.toString())).thenReturn(true)
        whenever(mockLettingAgentAccessService.isTokenAuthorisedInSession(token.toString())).thenReturn(true)

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects a valid but unauthorised token to the invitation journey`() {
        mockRequest.requestURI = propertyDetailsUri
        whenever(mockLettingAgentAccessService.getTokenIsValid(token.toString())).thenReturn(true)
        whenever(mockLettingAgentAccessService.isTokenAuthorisedInSession(token.toString())).thenReturn(false)

        assertFalse(callPreHandle())
        verify(mockResponse).sendRedirect("$LETTING_AGENT_INVITATION_ROUTE?token=$token")
    }

    @Test
    fun `preHandle redirects an invalid token to the invalid-link page and prunes it from the session`() {
        mockRequest.requestURI = propertyDetailsUri
        whenever(mockLettingAgentAccessService.getTokenIsValid(token.toString())).thenReturn(false)

        assertFalse(callPreHandle())
        verify(mockLettingAgentAccessService).removeAuthorisedTokenFromSession(token.toString())
        verify(mockResponse).sendRedirect(LETTING_AGENT_INVALID_LINK_ROUTE)
    }

    @Test
    fun `preHandle redirects to invalid-link when the URL has no property-details token segment`() {
        mockRequest.requestURI = "/landlord/letting-agent/property-details"

        assertFalse(callPreHandle())
        verify(mockLettingAgentAccessService, never()).removeAuthorisedTokenFromSession(anyString())
        verify(mockResponse).sendRedirect(LETTING_AGENT_INVALID_LINK_ROUTE)
    }

    @Test
    fun `preHandle allows an invitation-journey step whose journey token is still valid`() {
        val journeyId = "journey-123"
        mockRequest.requestURI = "$LETTING_AGENT_INVITATION_ROUTE/enter-password"
        mockRequest.setParameter(JourneyIdProvider.PARAMETER_NAME, journeyId)
        whenever(mockLettingAgentAccessService.getInvitationTokenForJourneyIdFromSessionOrNull(journeyId)).thenReturn(token.toString())
        whenever(mockLettingAgentAccessService.getTokenIsValid(token.toString())).thenReturn(true)

        assertTrue(callPreHandle())
        verify(mockResponse, never()).sendRedirect(anyString())
    }

    @Test
    fun `preHandle redirects an invitation-journey step to invalid-link when the journey token has been revoked`() {
        val journeyId = "journey-123"
        mockRequest.requestURI = "$LETTING_AGENT_INVITATION_ROUTE/enter-password"
        mockRequest.setParameter(JourneyIdProvider.PARAMETER_NAME, journeyId)
        whenever(mockLettingAgentAccessService.getInvitationTokenForJourneyIdFromSessionOrNull(journeyId)).thenReturn(token.toString())
        whenever(mockLettingAgentAccessService.getTokenIsValid(token.toString())).thenReturn(false)

        assertFalse(callPreHandle())
        verify(mockLettingAgentAccessService).removeAuthorisedTokenFromSession(token.toString())
        verify(mockResponse).sendRedirect(LETTING_AGENT_INVALID_LINK_ROUTE)
    }

    @Test
    fun `preHandle redirects an invitation-journey step to invalid-link when no token is held for the journey`() {
        val journeyId = "journey-123"
        mockRequest.requestURI = "$LETTING_AGENT_INVITATION_ROUTE/enter-password"
        mockRequest.setParameter(JourneyIdProvider.PARAMETER_NAME, journeyId)
        whenever(mockLettingAgentAccessService.getInvitationTokenForJourneyIdFromSessionOrNull(journeyId)).thenReturn(null)

        assertFalse(callPreHandle())
        verify(mockLettingAgentAccessService, never()).removeAuthorisedTokenFromSession(anyString())
        verify(mockResponse).sendRedirect(LETTING_AGENT_INVALID_LINK_ROUTE)
    }
}
