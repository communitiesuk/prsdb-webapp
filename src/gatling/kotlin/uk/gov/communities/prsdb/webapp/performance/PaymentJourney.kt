package uk.gov.communities.prsdb.webapp.performance

import java.net.URI

object PaymentJourney {
    fun baseUrl(
        value: String,
        target: PerformanceTarget,
        applicationBaseUrl: String,
    ): String =
        when (target) {
            PerformanceTarget.LOCAL -> localBaseUrl(value)
            PerformanceTarget.NFT -> {
                val uri = URI.create(value)
                val application = URI.create(applicationBaseUrl)
                require(
                    uri.scheme == "https" && uri.host == application.host && uri.port == application.port &&
                        uri.rawPath == "/local/gov-uk-pay" && uri.userInfo == null && uri.rawQuery == null && uri.fragment == null,
                ) { "NFT payment mock must use the configured application origin and /local/gov-uk-pay path" }
                value
            }
        }

    fun hostedUrl(
        location: String,
        paymentBaseUrl: String,
    ): String {
        val result = HttpDestination.resolve(location, paymentBaseUrl)
        val uri = URI.create(result)
        val base = URI.create(paymentBaseUrl)
        val path = base.path.trimEnd('/')
        require(uri.path.matches(Regex("${Regex.escape(path)}/(?:mock-card-page|card)/[A-Za-z0-9_-]+"))) {
            "Unexpected hosted payment path: ${uri.path}"
        }
        require(uri.rawQuery == null && uri.fragment == null) { "Unexpected hosted payment query" }
        return result
    }

    fun localBaseUrl(value: String): String {
        val uri = URI.create(value)
        require(uri.scheme == "http" && uri.host in setOf("localhost", "127.0.0.1", "::1")) {
            "Registration payment fixtures must use an explicitly configured loopback HTTP dependency"
        }
        require(uri.userInfo == null && uri.rawQuery == null && uri.fragment == null)
        return value.trimEnd('/')
    }
}
