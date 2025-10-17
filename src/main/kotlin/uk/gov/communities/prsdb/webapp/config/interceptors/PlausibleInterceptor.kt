package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView

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
        val plausibleEventReferrer = request.getHeader("Referer")
        println("plausibleEventReferrer: $plausibleEventReferrer")
        val plausibleEventCurrentUrl = request.requestURL.toString()
        modelAndView?.modelMap?.addAttribute("plausibleEventReferrer", plausibleEventReferrer)
        modelAndView?.modelMap?.addAttribute("plausibleEventCurrentUrl", plausibleEventCurrentUrl)
    }
}
