package uk.gov.communities.prsdb.webapp.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestClientResponseException
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException

class GovUkPayConfigTests {
    @Test
    fun `govUkPayRestClient sends requests to the configured base URL with the API key as a bearer token`() {
        // Arrange
        val config =
            GovUkPayConfig().apply {
                baseUrl = BASE_URL
                apiKey = API_KEY
            }
        // mutate() copies the base URL and interceptors so the mock server sees exactly what the bean would send
        val builder = config.govUkPayRestClient().mutate()
        val mockServer = MockRestServiceServer.bindTo(builder).build()
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer $API_KEY"))
            .andRespond(withSuccess())

        // Act
        builder
            .build()
            .get()
            .uri("/v1/payments/{paymentId}", PAYMENT_ID)
            .retrieve()
            .toBodilessEntity()

        // Assert
        mockServer.verify()
    }

    @Test
    fun `govUkPayRestClient throws GovUkPayException for a redirect response`() {
        // Arrange
        val builder = createConfig().govUkPayRestClient().mutate()
        val mockServer = MockRestServiceServer.bindTo(builder).build()
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(withStatus(HttpStatus.FOUND))

        // Act
        val exception =
            assertThrows(GovUkPayException::class.java) {
                builder
                    .build()
                    .get()
                    .uri("/v1/payments/{paymentId}", PAYMENT_ID)
                    .retrieve()
                    .toBodilessEntity()
            }

        // Assert
        assertEquals("GOV.UK Pay responded with unexpected HTTP status 302", exception.message)
        mockServer.verify()
    }

    @Test
    fun `govUkPayRestClient leaves error responses to the default RestClient error handling`() {
        // Arrange
        val builder = createConfig().govUkPayRestClient().mutate()
        val mockServer = MockRestServiceServer.bindTo(builder).build()
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS))

        // Act
        val exception =
            assertThrows(RestClientResponseException::class.java) {
                builder
                    .build()
                    .get()
                    .uri("/v1/payments/{paymentId}", PAYMENT_ID)
                    .retrieve()
                    .toBodilessEntity()
            }

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), exception.statusCode.value())
        mockServer.verify()
    }

    private fun createConfig() =
        GovUkPayConfig().apply {
            baseUrl = BASE_URL
            apiKey = API_KEY
        }

    companion object {
        private const val BASE_URL = "https://gov-uk-pay.test"
        private const val API_KEY = "test-api-key"
        private const val PAYMENT_ID = "hu20sqlact5260q2nanm0q8u93"
    }
}
