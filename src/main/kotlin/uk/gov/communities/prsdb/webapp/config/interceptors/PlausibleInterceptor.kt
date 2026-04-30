package uk.gov.communities.prsdb.webapp.config.interceptors

import jakarta.servlet.RequestDispatcher
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.ModelAndView
import org.springframework.web.servlet.view.RedirectView
import java.net.URI
import java.net.URISyntaxException

class PlausibleInterceptor : HandlerInterceptor {
    override fun postHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
        modelAndView: ModelAndView?,
    ) {
        if (modelAndView == null) return
        if (modelAndView.view is RedirectView ||
            modelAndView.viewName?.startsWith("redirect:") == true ||
            modelAndView.viewName?.startsWith("forward:") == true
        ) {
            return
        }

        modelAndView.modelMap.addAttribute(CURRENT_URL_ATTR, resolveCurrentUrl(request))
        modelAndView.modelMap.addAttribute(REFERRER_ATTR, resolveReferrer(request))
    }

    private fun resolveCurrentUrl(request: HttpServletRequest): String {
        val errorUri = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI) as? String
        return errorUri ?: request.requestURI
    }

    private fun resolveReferrer(request: HttpServletRequest): String {
        val refererHeader = request.getHeader("Referer") ?: return EXTERNAL
        return try {
            // The Referer header is supplied by the client and may be malformed
            // (e.g. invalid percent-encoding, bad characters). URI(String) throws
            // URISyntaxException for any value that does not parse per RFC 2396;
            // we treat such values as external rather than propagating a 500.
            val refererUri = URI(refererHeader)
            // Require an explicit host that matches the request server. Hostless,
            // bare-token, or cross-origin referrers are all reported as "external".
            if (refererUri.host == null || !refererUri.host.equals(request.serverName, ignoreCase = true)) {
                EXTERNAL
            } else {
                val path = refererUri.path
                if (path.isNullOrEmpty()) "/" else path
            }
        } catch (_: URISyntaxException) {
            EXTERNAL
        }
    }

    companion object {
        const val CURRENT_URL_ATTR = "plausibleEventCurrentUrl"
        const val REFERRER_ATTR = "plausibleEventReferrer"
        private const val EXTERNAL = "external"
    }
}
