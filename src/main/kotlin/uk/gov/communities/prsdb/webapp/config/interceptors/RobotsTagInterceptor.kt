package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor

class RobotsTagInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        response.setHeader(X_ROBOTS_TAG_HEADER, X_ROBOTS_TAG_VALUE)
        return true
    }

    companion object {
        const val X_ROBOTS_TAG_HEADER = "X-Robots-Tag"
        const val X_ROBOTS_TAG_VALUE = "noindex, nofollow"
    }
}
