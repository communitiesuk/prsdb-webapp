package uk.gov.communities.prsdb.webapp.services

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpSession
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import uk.gov.communities.prsdb.webapp.annotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.BACK_URL_STORAGE_SESSION_ATTRIBUTE
import kotlin.math.abs

@PrsdbWebService
class BackUrlStorageService(
    val session: HttpSession,
) {
    fun storeCurrentUrlReturningKey(): Int {
        val currentUrl = getCurrentUrl() ?: throw IllegalStateException("Current URL is null")
        val currentUrlHash = abs(currentUrl.hashCode())
        val backUrlMap = session.getAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE).toBackUrlMap()

        val storedUrl = backUrlMap[currentUrlHash]
        return when (storedUrl) {
            currentUrl -> currentUrlHash
            null -> {
                session.setAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE, backUrlMap + (currentUrlHash to currentUrl))
                currentUrlHash
            }
            else -> storeUrlWithHashCollisionReturningKey(backUrlMap, currentUrl)
        }
    }

    private fun storeUrlWithHashCollisionReturningKey(
        backUrlMap: Map<Int, String>,
        currentUrl: String,
    ): Int {
        val existingUrlKey = backUrlMap.entries.firstOrNull { it.value == currentUrl }?.key
        if (existingUrlKey != null) {
            return existingUrlKey
        } else {
            var newKey = 1
            while (backUrlMap.containsKey(newKey)) {
                newKey++
            }
            session.setAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE, backUrlMap + (newKey to currentUrl))
            return newKey
        }
    }

    fun getBackUrl(urlKey: Int): String? {
        val backUrlMap = session.getAttribute(BACK_URL_STORAGE_SESSION_ATTRIBUTE).toBackUrlMap()
        return backUrlMap[urlKey]
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

    private fun Any?.toBackUrlMap(): Map<Int, String> {
        if (this == null) return emptyMap()
        val initialMap: Map<*, *> = this as? Map<*, *> ?: return emptyMap()
        return initialMap
            .map { (key, value) -> (key as? Int ?: return emptyMap()) to (value as? String ?: return emptyMap()) }
            .associate { it }
    }
}
