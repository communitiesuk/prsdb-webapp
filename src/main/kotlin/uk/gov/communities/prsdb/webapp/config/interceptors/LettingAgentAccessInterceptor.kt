package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_DETAILS_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.TOKEN
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVALID_LINK_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController.Companion.LETTING_AGENT_INVITATION_ROUTE
import uk.gov.communities.prsdb.webapp.services.LettingAgentAccessService

// PDJB-1659: Access is granted to the SESSION (a validated invitation token held in the session), not
// to a Spring Authentication / ROLE_LETTING_AGENT, because the letting agent is not a distinct system
// user. This interceptor is the enforcement point for every letting-agent route except the invitation
// (set/enter password) journey, which is excluded in LettingAgentAccessInterceptorConfig.
class LettingAgentAccessInterceptor(
    private val lettingAgentAccessService: LettingAgentAccessService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val token = extractToken(request.requestURI)

        if (token == null || !lettingAgentAccessService.getTokenIsValid(token)) {
            if (token != null) {
                lettingAgentAccessService.removeAuthorisedTokenFromSession(token)
            }
            response.sendRedirect(LETTING_AGENT_INVALID_LINK_ROUTE)
            return false
        }

        if (lettingAgentAccessService.isTokenAuthorisedInSession(token)) {
            return true
        }

        response.sendRedirect("$LETTING_AGENT_INVITATION_ROUTE?$TOKEN=$token")
        return false
    }

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
