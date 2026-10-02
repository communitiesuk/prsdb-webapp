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
}
