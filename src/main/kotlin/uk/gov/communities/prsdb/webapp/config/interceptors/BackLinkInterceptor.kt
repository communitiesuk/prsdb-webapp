package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_ATTR_NAME
import uk.gov.communities.prsdb.webapp.constants.WITH_BACK_URL_PARAMETER_NAME
import java.util.Optional

class BackLinkInterceptor(
    private val retrieveBackUrl: (Int) -> String?,
) : HandlerInterceptor {
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        val backUrlKey = getBackUrlParameter(request)

        if (modelAndView?.view is RedirectView ||
            modelAndView?.viewName?.startsWith("redirect:") == true ||
            modelAndView?.viewName?.startsWith("forward:") == true
        ) {
            modelAndView.modelMap.addAttribute(WITH_BACK_URL_PARAMETER_NAME, backUrlKey)
            return
        }

        if (backUrlKey != null) {
            val backUrl = retrieveBackUrl(backUrlKey)
            if (backUrl != null) {
                modelAndView?.modelMap?.addAttribute(BACK_URL_ATTR_NAME, backUrl)
            }
        }
    }

    private fun getBackUrlParameter(request: HttpServletRequest): Int? = request.getParameter(WITH_BACK_URL_PARAMETER_NAME)?.toIntOrNull()

    companion object {
        fun String.overrideBackLinkForUrl(backUrlKey: Int?): String =
            UriComponentsBuilder
                .fromUriString(this)
                .queryParamIfPresent(WITH_BACK_URL_PARAMETER_NAME, Optional.ofNullable(backUrlKey))
                .build(true)
                .toUriString()
    }
}
