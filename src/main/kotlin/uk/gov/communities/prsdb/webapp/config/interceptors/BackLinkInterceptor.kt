package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.WITH_BACK_URL_PARAMETER_NAME

class BackLinkInterceptor(
    private val backProvider: BackLinkProvider,
) : HandlerInterceptor {
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        val backDestination = getBackUrlParameter(request)
        if (backDestination != null) {
            val backUrl = backProvider.getBackUrl(backDestination)
            if (backUrl != null) {
                modelAndView?.modelMap?.addAttribute(BACK_URL_ATTR_NAME, backUrl)
            }
        }
    }

    private fun getBackUrlParameter(request: HttpServletRequest): Int? = request.getParameter(WITH_BACK_URL_PARAMETER_NAME)?.toIntOrNull()

    fun interface BackLinkProvider {
        fun getBackUrl(destination: Int): String?
    }
}
