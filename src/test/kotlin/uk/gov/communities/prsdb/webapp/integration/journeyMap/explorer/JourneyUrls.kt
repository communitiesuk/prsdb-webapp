package uk.gov.communities.prsdb.webapp.integration.journeyMap.explorer

import uk.gov.communities.prsdb.webapp.journeys.JourneyIdProvider
import java.net.URI

class JourneyUrls(
    basePath: String,
) {
    private val base = basePath.trimEnd('/')

    fun stepPathOf(url: String): String? {
        val uri = parse(url) ?: return null
        if (!isLocal(uri)) return null
        val path = uri.path ?: return null
        return if (path.startsWith("$base/")) path.removePrefix("$base/").trimEnd('/').ifEmpty { null } else null
    }

    fun pathOf(url: String): String = parse(url)?.path ?: url

    fun journeyIdOf(url: String): String? =
        parse(url)
            ?.rawQuery
            ?.split("&")
            ?.firstOrNull { it.startsWith("${JourneyIdProvider.PARAMETER_NAME}=") }
            ?.substringAfter("=")

    fun isLocal(url: String) = parse(url)?.let { isLocal(it) } ?: false

    fun linkKey(url: String): String {
        val query =
            parse(url)
                ?.rawQuery
                ?.split("&")
                ?.filterNot { it.startsWith("${JourneyIdProvider.PARAMETER_NAME}=") }
                ?.sorted()
                ?.joinToString("&")
                .orEmpty()
        return "link:" + (stepPathOf(url) ?: pathOf(url)) + if (query.isEmpty()) "" else "?$query"
    }

    private fun isLocal(uri: URI) = uri.host == null || uri.host in LOCAL_HOSTS

    private fun parse(url: String): URI? = runCatching { URI(url) }.getOrNull()

    companion object {
        private val LOCAL_HOSTS = setOf("localhost", "127.0.0.1")
    }
}
