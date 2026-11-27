package uk.gov.communities.prsdb.webapp.performance

import java.net.URI

object PaymentJourney {
    private const val MOCK_PATH = "/local/gov-uk-pay"

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
                        uri.rawPath == MOCK_PATH && uri.rawUserInfo == null && uri.rawQuery == null && uri.fragment == null,
                ) { "NFT payment mock must use the configured application origin and $MOCK_PATH path" }
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
        require(uri.path.matches(Regex("${Regex.escape(path)}/mock-card-page/[A-Za-z0-9_-]+"))) {
            "Unexpected hosted payment path: ${uri.path}"
        }
        require(uri.rawQuery == null && uri.rawFragment == null) { "Unexpected hosted payment query or fragment" }
        return result
    }

    fun localBaseUrl(value: String): String {
        val uri = URI.create(value)
        require(uri.scheme == "http" && uri.host in setOf("localhost", "127.0.0.1", "::1")) {
            "Registration payment fixtures must use an explicitly configured loopback HTTP dependency"
        }
        require(uri.rawPath == MOCK_PATH) { "Registration payment fixtures must use $MOCK_PATH" }
        require(uri.rawUserInfo == null && uri.rawQuery == null && uri.rawFragment == null)
        return value.trimEnd('/')
    }
}
