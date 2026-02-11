package uk.gov.communities.prsdb.webapp.testHelpers.mockObjects

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Optional
import javax.net.ssl.SSLSession

class MockHttpResponse<T>(
    private val statusCode: Int = 200,
    private val body: T? = null,
    private val headers: Map<String, List<String>> = emptyMap(),
) : HttpResponse<T> {
    override fun body(): T? = body

    override fun statusCode(): Int = statusCode

    override fun request(): HttpRequest? = null

    override fun previousResponse(): Optional<HttpResponse<T>> = Optional.empty()

    override fun headers(): HttpHeaders? = HttpHeaders.of(headers) { _, _ -> true }

    override fun sslSession(): Optional<SSLSession> = Optional.empty()

    override fun uri(): URI? = null

    override fun version(): HttpClient.Version? = null
}
