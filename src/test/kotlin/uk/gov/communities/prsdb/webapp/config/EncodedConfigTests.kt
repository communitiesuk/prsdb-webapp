package uk.gov.communities.prsdb.webapp.config

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestTemplate
import uk.gov.communities.prsdb.webapp.exceptions.EncodedException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.http.HttpTimeoutException
import java.nio.charset.StandardCharsets
import java.time.Duration

class EncodedConfigTests {
    // Binding to a RestTemplate exposes the mock server's request factory, so the token and API requests can share it
    private val restTemplate = RestTemplate()
    private val mockServer = MockRestServiceServer.bindTo(restTemplate).build()
    private val mockRequestFactory = restTemplate.requestFactory

    @Test
    fun `encodedRestClient requests an access token with the Encoded client credentials`() {
        // Arrange
        val expectedAuthorization = "Basic ${HttpHeaders.encodeBasicAuth(CLIENT_ID, CLIENT_SECRET, StandardCharsets.UTF_8)}"
        mockServer
            .expect(requestTo(TOKEN_URI))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, expectedAuthorization))
            .andExpect(content().formDataContains(mapOf("grant_type" to "client_credentials", "scope" to "read write")))
            .andRespond(tokenResponse())
        mockServer.expect(requestTo("$BASE_URL/orders")).andRespond(withSuccess())

        // Act
        getOrders(createConfig().createRestClient(mockRequestFactory))

        // Assert
        mockServer.verify()
    }

    @Test
    fun `encodedRestClient sends requests to the configured base URL with the access token as a bearer token`() {
        // Arrange
        mockServer.expect(requestTo(TOKEN_URI)).andRespond(tokenResponse())
        mockServer
            .expect(requestTo("$BASE_URL/orders"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $ACCESS_TOKEN"))
            .andRespond(withSuccess())

        // Act
        getOrders(createConfig().createRestClient(mockRequestFactory))

        // Assert
        mockServer.verify()
    }

    @Test
    fun `encodedRestClient reuses the access token for later requests`() {
        // Arrange
        mockServer.expect(ExpectedCount.once(), requestTo(TOKEN_URI)).andRespond(tokenResponse())
        mockServer
            .expect(ExpectedCount.twice(), requestTo("$BASE_URL/orders"))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $ACCESS_TOKEN"))
            .andRespond(withSuccess())
        val client = createConfig().createRestClient(mockRequestFactory)

        // Act
        getOrders(client)
        getOrders(client)

        // Assert
        mockServer.verify()
    }

    @Test
    fun `encodedRestClient throws EncodedException without sending the request when the access token request fails`() {
        // Arrange
        mockServer
            .expect(requestTo(TOKEN_URI))
            .andRespond(
                withStatus(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("""{"error":"invalid_client"}"""),
            )

        // Act
        val exception =
            assertThrows(EncodedException::class.java) {
                getOrders(createConfig().createRestClient(mockRequestFactory))
            }

        // Assert
        assertEquals("Failed to get an Encoded access token: invalid_client", exception.message)
        assertInstanceOf(OAuth2AuthorizationException::class.java, exception.cause)
        mockServer.verify()
    }

    @Test
    fun `encodedRestClient throws EncodedException without sending the request when no authorised client is returned`() {
        // Arrange
        val authorizationCodeRegistration =
            ClientRegistration
                .withRegistrationId(EncodedConfig.CLIENT_REGISTRATION_ID)
                .clientId(CLIENT_ID)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationUri("https://encoded.test/auth2/oauth2/authorize")
                .redirectUri("https://prsdb.test/callback")
                .tokenUri(TOKEN_URI)
                .build()

        // Act
        val exception =
            assertThrows(EncodedException::class.java) {
                getOrders(createConfig(authorizationCodeRegistration).createRestClient(mockRequestFactory))
            }

        // Assert
        assertEquals("Failed to get an Encoded access token: no authorised client was returned", exception.message)
        mockServer.verify()
    }

    @Test
    fun `encodedRestClient throws EncodedException for a redirect response`() {
        // Arrange
        mockServer.expect(requestTo(TOKEN_URI)).andRespond(tokenResponse())
        mockServer.expect(requestTo("$BASE_URL/orders")).andRespond(withStatus(HttpStatus.FOUND))

        // Act
        val exception =
            assertThrows(EncodedException::class.java) {
                getOrders(createConfig().createRestClient(mockRequestFactory))
            }

        // Assert
        assertEquals("Encoded responded with unexpected HTTP status 302", exception.message)
        mockServer.verify()
    }

    @Test
    fun `encodedRestClient leaves error responses to the default RestClient error handling`() {
        // Arrange
        mockServer.expect(requestTo(TOKEN_URI)).andRespond(tokenResponse())
        mockServer.expect(requestTo("$BASE_URL/orders")).andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR))

        // Act
        val exception =
            assertThrows(RestClientResponseException::class.java) {
                getOrders(createConfig().createRestClient(mockRequestFactory))
            }

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.statusCode.value())
        mockServer.verify()
    }

    @Test
    fun `encodedRestClient stops waiting for an access token after the read timeout`() {
        // Arrange
        ServerSocket(0).use { unresponsiveServer ->
            val client =
                createConfig(clientRegistration(tokenUri = "http://127.0.0.1:${unresponsiveServer.localPort}/token"))
                    .apply { readTimeout = Duration.ofMillis(100) }
                    .encodedRestClient()

            // Act
            val exception =
                assertTimeoutPreemptively(Duration.ofSeconds(5)) {
                    assertThrows(EncodedException::class.java) { getOrders(client) }
                }

            // Assert
            assertInstanceOf(HttpTimeoutException::class.java, NestedExceptionUtils.getMostSpecificCause(exception))
        }
    }

    @Test
    fun `encodedRestClient stops waiting for Encoded to respond after the read timeout`() {
        // Arrange
        withTokenServer { tokenUri ->
            ServerSocket(0).use { unresponsiveServer ->
                val client =
                    createConfig(clientRegistration(tokenUri))
                        .apply {
                            baseUrl = "http://127.0.0.1:${unresponsiveServer.localPort}"
                            readTimeout = Duration.ofMillis(100)
                        }.encodedRestClient()

                // Act
                val exception =
                    assertTimeoutPreemptively(Duration.ofSeconds(5)) {
                        assertThrows(ResourceAccessException::class.java) { getOrders(client) }
                    }

                // Assert
                assertInstanceOf(HttpTimeoutException::class.java, exception.cause)
            }
        }
    }

    private fun getOrders(client: RestClient) {
        client
            .get()
            .uri("/orders")
            .retrieve()
            .toBodilessEntity()
    }

    private fun createConfig(clientRegistration: ClientRegistration = clientRegistration()) =
        EncodedConfig(InMemoryClientRegistrationRepository(clientRegistration)).apply {
            baseUrl = BASE_URL
            connectTimeout = Duration.ofSeconds(1)
            readTimeout = Duration.ofSeconds(1)
        }

    private fun clientRegistration(tokenUri: String = TOKEN_URI) =
        ClientRegistration
            .withRegistrationId(EncodedConfig.CLIENT_REGISTRATION_ID)
            .clientId(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("read", "write")
            .tokenUri(tokenUri)
            .build()

    private fun tokenResponse() = withSuccess(TOKEN_RESPONSE, MediaType.APPLICATION_JSON)

    private fun withTokenServer(action: (tokenUri: String) -> Unit) {
        val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/token") { exchange ->
            exchange.requestBody.use { it.readAllBytes() }
            val bytes = TOKEN_RESPONSE.toByteArray()
            exchange.responseHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            exchange.sendResponseHeaders(HttpStatus.OK.value(), bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()

        try {
            action("http://127.0.0.1:${server.address.port}/token")
        } finally {
            server.stop(0)
        }
    }

    companion object {
        private const val BASE_URL = "https://encoded.test/api/v1"
        private const val TOKEN_URI = "https://encoded.test/auth2/oauth2/token"
        private const val CLIENT_ID = "encoded-client-id"
        private const val CLIENT_SECRET = "encoded-client-secret" // pragma: allowlist secret
        private const val ACCESS_TOKEN = "encoded-access-token"
        private const val TOKEN_RESPONSE = """{"access_token":"$ACCESS_TOKEN","token_type":"Bearer","expires_in":3600}"""
    }
}
