package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.constants.ASSETS_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.HEALTHCHECK_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.constants.MAINTENANCE_PATH_SEGMENT

class MaintenanceInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.requestURI.contains(ASSETS_PATH_SEGMENT) ||
            request.requestURI.contains(MAINTENANCE_PATH_SEGMENT) ||
            request.requestURI.contains(HEALTHCHECK_PATH_SEGMENT)
        ) {
            return true
        } else {
            response.sendRedirect("/$MAINTENANCE_PATH_SEGMENT")
            return false
        }
    }
}
