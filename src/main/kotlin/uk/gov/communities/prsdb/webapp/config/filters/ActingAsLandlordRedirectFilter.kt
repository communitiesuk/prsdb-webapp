package uk.gov.communities.prsdb.webapp.config.filters

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.filter.OncePerRequestFilter
import uk.gov.communities.prsdb.webapp.constants.ACTING_USER_ID
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.SYSTEM_OPERATOR_PATH_SEGMENT

class ActingAsLandlordRedirectFilter : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.getSession(false)?.getAttribute(ACTING_USER_ID) == null ||
            request.requestURI.startsWith(LANDLORD_LOGIN_ROUTE_PREFIX)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val queryString = request.queryString?.let { "?$it" } ?: ""
        response.status = HttpServletResponse.SC_TEMPORARY_REDIRECT
        response.setHeader(HttpHeaders.LOCATION, "/$SYSTEM_OPERATOR_PATH_SEGMENT${request.requestURI}$queryString")
    }

    companion object {
        private const val LANDLORD_LOGIN_ROUTE_PREFIX = "/$LANDLORD_PATH_SEGMENT/login/"
    }
}
