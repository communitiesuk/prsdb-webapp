package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.communities.prsdb.webapp.services.ActiveInactiveService

class InactiveLandlordInterceptor(
    private val activeInactiveService: ActiveInactiveService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (activeInactiveService.isCurrentLandlordInactive()) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "An inactive landlord cannot update their landlord record")
        }
        return true
    }
}
