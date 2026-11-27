package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.filter.OncePerRequestFilter
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.services.ActingAsLandlordService

// Keeps a system operator who is acting as a landlord in the acting area when they follow an absolute /landlord/** link
class ActingAsLandlordRedirectFilter(
    private val actingAsLandlordService: ActingAsLandlordService,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.requestURI.startsWith(LANDLORD_LOGIN_ROUTE_PREFIX) || !actingAsLandlordService.isActingAsLandlord()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val queryString = request.queryString?.let { "?$it" } ?: ""
        // 307 rather than 302 so that form submissions are resent to the acting route with the same method and body
        response.status = HttpServletResponse.SC_TEMPORARY_REDIRECT
        response.setHeader(HttpHeaders.LOCATION, "/$SYSTEM_OPERATOR_PATH_SEGMENT${request.requestURI}$queryString")
    }

    companion object {
        // The One Login callback must complete on the landlord chain
        private const val LANDLORD_LOGIN_ROUTE_PREFIX = "/$LANDLORD_PATH_SEGMENT/login/"
    }
}
