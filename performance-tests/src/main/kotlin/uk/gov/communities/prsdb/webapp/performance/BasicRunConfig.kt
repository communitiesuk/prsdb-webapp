package uk.gov.communities.prsdb.webapp.performance

import java.net.URI
import java.net.URISyntaxException

enum class PerformanceTarget(
    val propertyValue: String,
) {
    LOCAL("local"),
    NFT("nft"),
}

enum class MeasurementMode(
    val propertyValue: String,
) {
    BASELINE("baseline"),
    GATED("gated"),
}

data class ResponseTimeLimits(
    val maxMs: Int,
    val meanMs: Int,
)

data class BasicRunConfig(
    val target: PerformanceTarget,
    val mode: MeasurementMode,
    val baseUrl: String,
    val simulatorUrl: String,
    val landlordSubject: String,
    val limits: ResponseTimeLimits?,
) {
    companion object {
        fun from(properties: Map<String, String>): BasicRunConfig {
            val targetValue = requiredProperty(properties, "gatling.target")
            val target =
                PerformanceTarget.entries.singleOrNull { it.propertyValue == targetValue }
                    ?: throw IllegalArgumentException("gatling.target must be local or nft")
            val modeValue = requiredProperty(properties, "gatling.basic.mode")
            val mode =
                MeasurementMode.entries.singleOrNull { it.propertyValue == modeValue }
                    ?: throw IllegalArgumentException("gatling.basic.mode must be baseline or gated")
            val maxKey = "gatling.${target.propertyValue}.basic.maxResponseTimeMs"
            val meanKey = "gatling.${target.propertyValue}.basic.meanResponseTimeMs"
            val limits =
                when (mode) {
                    MeasurementMode.BASELINE -> {
                        require(maxKey !in properties && meanKey !in properties) {
                            "Timing limits for ${target.propertyValue} require gatling.basic.mode=gated"
                        }
                        null
                    }
                    MeasurementMode.GATED -> {
                        val maxMs = positiveMilliseconds(properties, maxKey)
                        val meanMs = positiveMilliseconds(properties, meanKey)
                        require(meanMs <= maxMs) { "$meanKey must not exceed $maxKey" }
                        ResponseTimeLimits(maxMs, meanMs)
                    }
                }

            return BasicRunConfig(
                target = target,
                mode = mode,
                baseUrl = originUrl(properties, "gatling.baseUrl"),
                simulatorUrl = originUrl(properties, "gatling.simulatorUrl"),
                landlordSubject = requiredProperty(properties, "gatling.basic.landlordSubject"),
                limits = limits,
            )
        }

        private fun requiredProperty(
            properties: Map<String, String>,
            key: String,
        ): String {
            val value = properties[key]
            require(!value.isNullOrBlank()) { "$key must be provided and nonblank" }
            return value
        }

        private fun positiveMilliseconds(
            properties: Map<String, String>,
            key: String,
        ): Int {
            val value = requiredProperty(properties, key).toIntOrNull()
            require(value != null && value > 0) { "$key must be a positive integer number of milliseconds" }
            return value
        }

        private fun originUrl(
            properties: Map<String, String>,
            key: String,
        ): String {
            val value = requiredProperty(properties, key)
            val uri =
                try {
                    URI(value)
                } catch (_: URISyntaxException) {
                    throw IllegalArgumentException("$key must be a valid HTTP or HTTPS origin URL")
                }
            require(
                uri.scheme in setOf("http", "https") &&
                    !uri.host.isNullOrBlank() &&
                    uri.userInfo == null &&
                    uri.rawQuery == null &&
                    uri.rawFragment == null &&
                    uri.rawPath in setOf("", "/") &&
                    (uri.port == -1 || uri.port in 1..65535),
            ) { "$key must be an HTTP or HTTPS origin URL without credentials, path, query or fragment" }
            return value.removeSuffix("/")
        }
    }
}
