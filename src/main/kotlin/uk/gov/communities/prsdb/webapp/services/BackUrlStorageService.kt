package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.config.interceptors.BackLinkInterceptor

@Service
class BackUrlStorageService(
    val session: HttpSession,
) : BackLinkInterceptor.BackLinkProvider {
    fun rememberCurrentUrl(): Int {
        val currentUrl = getCurrentUrl() ?: throw IllegalStateException("Current URL is null")
        val backUrlMap = session.getAttribute("backUrlStorage").toBackUrlMapOrNull() ?: mapOf()
        val currentUrlEntry = backUrlMap.entries.firstOrNull { it.value == currentUrl }
        return if (currentUrlEntry != null) {
            currentUrlEntry.key
        } else {
            val nextKey = (backUrlMap.keys.maxOrNull() ?: 0) + 1
            session.setAttribute("backUrlStorage", backUrlMap + (nextKey to currentUrl))
            nextKey
        }
    }

    override fun getBackUrl(destination: Int): String? {
        val backUrlMap = session.getAttribute("backUrlStorage").toBackUrlMapOrNull() ?: mapOf()
        return backUrlMap[destination]
    }

    private fun getCurrentUrl(): String? {
        val context = RequestContextHolder.getRequestAttributes() as ServletRequestAttributes?
        if (context?.request is HttpServletRequest) {
            val uriSection = context.request.requestURI
            val queryString = context.request.queryString
            return uriSection + queryString?.let { "?$it" }.orEmpty()
        }
        return null
    }

    private fun Any?.toBackUrlMapOrNull(): Map<Int, String>? {
        if (this == null) return null
        val initialMap: Map<*, *> = this as? Map<*, *> ?: return null
        return initialMap.map { (key, value) -> (key as? Int ?: return null) to (value as? String ?: return null) }.associate { it }
    }
}
