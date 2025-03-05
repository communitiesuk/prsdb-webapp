package uk.gov.communities.prsdb.webapp.testHelpers

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Optional
import javax.net.ssl.SSLSession

class MockHttpResponse(
    private val statusCode: Int = 200,
    private val body: String? = null,
) : HttpResponse<String> {
    override fun body(): String? = body

    override fun statusCode(): Int = statusCode

    override fun request(): HttpRequest? = null

    override fun previousResponse(): Optional<HttpResponse<String>> = Optional.empty()

    override fun headers(): HttpHeaders? = null

    override fun sslSession(): Optional<SSLSession> = Optional.empty()

    override fun uri(): URI? = null

    override fun version(): HttpClient.Version? = null
}
