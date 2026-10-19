package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter
import uk.gov.communities.prsdb.webapp.config.security.ActingAsLandlordAuthenticationToken
import uk.gov.communities.prsdb.webapp.constants.ACTING_USER_ID
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.UserRolesService

class ActingAsLandlordFilter(
    private val userRolesService: UserRolesService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val operator = SecurityContextHolder.getContext().authentication
        if (operator !is OAuth2AuthenticationToken) {
            filterChain.doFilter(request, response)
            return
        }

        val actingUserId = request.getSession(false)?.getAttribute(ACTING_USER_ID) as? String
        if (actingUserId == null || operator.authorities.none { it.authority == ROLE_SYSTEM_OPERATOR }) {
            response.sendRedirect(SYSTEM_OPERATOR_DASHBOARD_URL)
            return
        }

        val actingContext = SecurityContextHolder.createEmptyContext()
        actingContext.authentication =
            ActingAsLandlordAuthenticationToken(
                actingUserId = actingUserId,
                landlordRoles = userRolesService.getLandlordRolesForSubjectId(actingUserId),
                operatorId = operator.name,
            )
        SecurityContextHolder.setContext(actingContext)

        filterChain.doFilter(request, response)
    }
}
