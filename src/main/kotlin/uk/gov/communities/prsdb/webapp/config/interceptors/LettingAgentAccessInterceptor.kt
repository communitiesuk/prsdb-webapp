package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVALID_LINK_ROUTE
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
        if (isInvitationJourneyRoute(request.requestURI)) {
            handleInvitationJourneyRequest(request, response)
        } else {
            handlePropertyAccessRequest(request, response)
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

        return !redirectToInvalidLinkIfTokenInvalid(token, response)
    }

    // Property-details pages carry the token in the URL and require the session to have been authorised for it.
    private fun handlePropertyAccessRequest(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Boolean {
        val token = extractToken(request.requestURI)

        if (redirectToInvalidLinkIfTokenInvalid(token, response)) {
            return false
        }

        if (lettingAgentAccessService.isTokenAuthorisedInSession(token!!)) {
            return true
        }

        response.sendRedirect("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=$token")
        return false
    }

    // Returns true (and redirects to the invalid-link page) if the token is missing or no longer valid, having
    // first pruned it from the session's authorised tokens. Returns false if the token is valid.
    private fun redirectToInvalidLinkIfTokenInvalid(
        token: String?,
        response: HttpServletResponse,
    ): Boolean {
        if (token == null || !lettingAgentAccessService.getTokenIsValid(token)) {
            if (token != null) {
                lettingAgentAccessService.removeAuthorisedTokenFromSession(token)
            }
            response.sendRedirect(LETTING_AGENT_INVALID_LINK_ROUTE)
            return true
        }
        return false
    }

    private fun isInvitationJourneyRoute(requestUri: String): Boolean =
        requestUri == LETTING_AGENT_INVITATION_ROUTE || requestUri.startsWith("$LETTING_AGENT_INVITATION_ROUTE/")

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
}
