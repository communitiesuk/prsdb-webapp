package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.LETTING_AGENT_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_INVALID_LINK_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

class LettingAgentAccessInterceptor(
    private val lettingAgentAccessService: LettingAgentAccessService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean =
        when {
            isInvitationJourneyRoute(request.requestURI) -> handleInvitationJourneyRequest(request, response)
            isPropertyAccessRoute(request.requestURI) -> handlePropertyAccessRequest(request, response)
            // Any other letting agent route is not one we recognise, so we send the user to the invalid-link page.
            else -> redirectToInvalidLink(response)
        }

    // The invitation journey (set/enter password) does not require the session to be authorised yet, but we
    // still check the token is valid so a revoked invitation is caught even on the password pages. The token
    // for these pages is not in the URL, so it is resolved from the session using the journeyId.
    private fun handleInvitationJourneyRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        val journeyId = request.getParameter(JourneyIdProvider.PARAMETER_NAME)
        val token = journeyId?.let { lettingAgentAccessService.getInvitationTokenForJourneyIdFromSessionOrNull(it) }

        return allowIfTokenValid(token, response)
    }

    // Property-details pages carry the token in the URL and require the session to have been authorised for it.
    private fun handlePropertyAccessRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        val token = extractToken(request.requestURI)

        if (!allowIfTokenValid(token, response)) {
            return false
        }

        if (lettingAgentAccessService.isTokenAuthorisedInSession(token!!)) {
            return true
        }

        response.sendRedirect("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=$token")
        return false
    }

    // Returns true if the token is present and valid, so the request may proceed. Otherwise it prunes the
    // token from the session's authorised tokens, redirects to the invalid-link page, and returns false.
    private fun allowIfTokenValid(
        token: String?,
        response: HttpServletResponse,
    ): Boolean {
        if (token != null && lettingAgentAccessService.getTokenIsValid(token)) {
            return true
        }
        if (token != null) {
            lettingAgentAccessService.removeAuthorisedTokenFromSession(token)
        }
        return redirectToInvalidLink(response)
    }

    private fun redirectToInvalidLink(response: HttpServletResponse): Boolean {
        response.sendRedirect(LETTING_AGENT_INVITATION_INVALID_LINK_ROUTE)
        return false
    }

    private fun isInvitationJourneyRoute(requestUri: String): Boolean =
        requestUri == LETTING_AGENT_INVITATION_ROUTE || requestUri.startsWith("$LETTING_AGENT_INVITATION_ROUTE/")

    private fun isPropertyAccessRoute(requestUri: String): Boolean = requestUri.startsWith(PROPERTY_DETAILS_ROUTE_PREFIX)

    // The token is the path segment immediately following the property-details segment. This covers both
    // /property-details/{token} and /property-details/{token}/update-.../{*stepPath} routes.
    private fun extractToken(requestUri: String): String? {
        val segments = requestUri.trim('/').split('/')
        val detailsIndex = segments.indexOf(PROPERTY_DETAILS_SEGMENT)
        if (detailsIndex == -1 || detailsIndex + 1 >= segments.size) {
            return null
        }
        return segments[detailsIndex + 1]
    }

    companion object {
        private const val PROPERTY_DETAILS_ROUTE_PREFIX =
            "/$LANDLORD_PATH_SEGMENT/$LETTING_AGENT_PATH_SEGMENT/$PROPERTY_DETAILS_SEGMENT/"
    }
}
