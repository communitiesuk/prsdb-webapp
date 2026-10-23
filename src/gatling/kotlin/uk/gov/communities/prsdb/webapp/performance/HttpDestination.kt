package uk.gov.communities.prsdb.webapp.performance

import java.net.URI

object HttpDestination {
    fun resolve(
        value: String,
        origin: String,
        relativeTo: String = origin,
    ): String {
        val allowed = URI.create(origin)
        val current = URI.create(relativeTo)
        val destination = if (value.isEmpty()) current else current.resolve(value)
        require(
            destination.scheme == allowed.scheme &&
                destination.host == allowed.host &&
                destination.port == allowed.port &&
                destination.rawUserInfo == null,
        ) { "Unexpected HTTP destination origin" }
        return destination.toString()
    }

    // CloudFront keeps every cacheable GET for at least 1s (min TTL), even when the app sends no-store, so a repeat
    // read of the same URL by the same session can return a stale page. The query string is part of the cache key.
    fun withCacheBuster(url: String): String = url + (if ('?' in url) "&" else "?") + "perfCacheBuster=" + System.nanoTime()
}
