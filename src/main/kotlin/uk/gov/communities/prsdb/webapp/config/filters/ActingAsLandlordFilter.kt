package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import uk.gov.communities.prsdb.webapp.config.security.ActingAsLandlordAuthenticationToken
import uk.gov.communities.prsdb.webapp.constants.ROLE_SYSTEM_OPERATOR
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService
import uk.gov.communities.prsdb.webapp.services.UserRolesService

class ActingAsLandlordFilter(
    private val actingAsLandlordService: ActingAsLandlordService,
    private val userRolesService: UserRolesService,
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val user = SecurityContextHolder.getContext().authentication
        if (user == null || !user.isAuthenticated) {
            filterChain.doFilter(request, response)
            return
        }

        val isSystemOperator = user.authorities.any { it.authority == ROLE_SYSTEM_OPERATOR }
        val actingAsUserId = if (isSystemOperator) actingAsLandlordService.getActingAsUserId() else null
        if (actingAsUserId == null) {
            response.sendRedirect(SYSTEM_OPERATOR_DASHBOARD_URL)
            return
        }

        // The acting context only lives in SecurityContextHolder for this request, and is never saved to the session
        val actingContext = SecurityContextHolder.createEmptyContext()
        actingContext.authentication =
            ActingAsLandlordAuthenticationToken(
                actingAsUserId = actingAsUserId,
                landlordRoles = userRolesService.getLandlordRolesForSubjectId(actingAsUserId),
                systemOperatorId = user.name,
            )
        SecurityContextHolder.setContext(actingContext)

        filterChain.doFilter(request, response)
    }
}
