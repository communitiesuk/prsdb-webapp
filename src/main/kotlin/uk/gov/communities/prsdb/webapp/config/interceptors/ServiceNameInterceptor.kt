package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.LOCAL_COUNCIL_PATH_SEGMENT

class ServiceNameInterceptor(
    private val localCouncilServiceName: String,
) : HandlerInterceptor {
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        if (request.requestURI.startsWith("/$LOCAL_COUNCIL_PATH_SEGMENT") &&
            modelAndView != null &&
            modelAndView.viewName?.startsWith("redirect:") != true
        ) {
            modelAndView.addObject("customServiceName", localCouncilServiceName)
        }
    }
}
