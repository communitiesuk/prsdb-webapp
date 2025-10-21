package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import java.net.URI

class PlausibleInterceptor : HandlerInterceptor {
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        val viewName = modelAndView?.viewName
        if (viewName?.startsWith("redirect:") == true || viewName?.startsWith("forward:") == true) {
            return
        }

        val plausibleEventCurrentUrl = request.requestURI + (request.queryString?.let { "?$it" } ?: "")

        val referrerHeader = request.getHeader("Referer")
        val plausibleEventReferrer =
            if (referrerHeader != null) {
                try {
                    val uri = URI(referrerHeader)
                    uri.path + (uri.query?.let { "?$it" } ?: "")
                } catch (e: Exception) {
                    "external"
                }
            } else {
                "external"
            }
        modelAndView?.modelMap?.addAttribute("plausibleEventReferrer", plausibleEventReferrer)
        modelAndView?.modelMap?.addAttribute("plausibleEventCurrentUrl", plausibleEventCurrentUrl)
    }
}
