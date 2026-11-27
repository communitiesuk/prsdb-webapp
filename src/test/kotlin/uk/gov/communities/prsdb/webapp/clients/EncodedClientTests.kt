package uk.gov.communities.prsdb.webapp.clients

import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import uk.gov.communities.prsdb.webapp.exceptions.EncodedException
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedCreateOrderRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedGovUkPayAttributes
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrder
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrderAttributes
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrderLinks
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedPayNowOptions
import java.io.IOException
import java.math.BigDecimal

class EncodedClientTests {
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var encodedClient: EncodedClient

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(BASE_URL)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        encodedClient = EncodedClient(builder.build())
    }

    @Test
    fun `createOrder posts the order as a single-element JSON array`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/orders"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.APPLICATION_JSON_VALUE)))
            .andExpect(header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
            .andExpect(
                content().json(
                    """
                    [
                        {
                            "object": "order",
                            "ref": "$REFERENCE",
                            "description": "$DESCRIPTION",
                            "currency": "GBP",
                            "totalAmount": 65.00,
                            "attributes": {
                                "govUkPay": {
                                    "paymentId": "$GOV_UK_PAY_PAYMENT_ID",
                                    "oneTimeToken": "$ONE_TIME_TOKEN"
                                }
                            },
                            "paynow": {
                                "returnUrl": "$RETURN_URL"
                            }
                        }
                    ]
                    """,
                    JsonCompareMode.STRICT,
                ),
            )
            // Encoded expects major currency units, so the amount must keep both decimal places
            .andExpect(content().string(containsString("\"totalAmount\":65.00")))
            .andRespond(createdResponse())

        // Act
        encodedClient.createOrder(createOrderRequest())

        // Assert
        mockServer.verify()
    }

    @Test
    fun `createOrder returns the created order`() {
        // Arrange
        mockServer.expect(requestTo("$BASE_URL/orders")).andRespond(createdResponse())

        // Act
        val order = encodedClient.createOrder(createOrderRequest())

        // Assert
        assertEquals(EncodedOrder(id = ORDER_ID, ref = REFERENCE, links = EncodedOrderLinks(paynow = PAYNOW_URL)), order)
    }

    @Test
    fun `createOrder returns an order without a PayNow link when Encoded does not return one`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/orders"))
            .andRespond(createdResponse("""[{"object":"order","id":"$ORDER_ID"}]"""))

        // Act
        val order = encodedClient.createOrder(createOrderRequest())

        // Assert
        assertEquals(ORDER_ID, order.id)
        assertNull(order.links.paynow)
    }

    @Test
    fun `createOrder throws EncodedException when Encoded responds with a success status other than 201`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/orders"))
            .andRespond(withStatus(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON).body(CREATED_ORDERS_JSON))

        // Act
        val exception = assertThrows(EncodedException::class.java) { encodedClient.createOrder(createOrderRequest()) }

        // Assert
        assertEquals("Encoded responded to create order with HTTP status 200 instead of 201", exception.message)
    }

    @ParameterizedTest
    @EnumSource(value = HttpStatus::class, names = ["BAD_REQUEST", "UNAUTHORIZED", "INTERNAL_SERVER_ERROR"])
    fun `createOrder throws EncodedException with the HTTP status for an error response`(status: HttpStatus) {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/orders"))
            .andRespond(withStatus(status).contentType(MediaType.APPLICATION_JSON).body("""{"error":"failed"}"""))

        // Act
        val exception = assertThrows(EncodedException::class.java) { encodedClient.createOrder(createOrderRequest()) }

        // Assert
        assertEquals(status.value(), exception.httpStatus?.value())
        assertEquals("Encoded request failed with HTTP status ${status.value()}", exception.message)
        assertInstanceOf(RestClientResponseException::class.java, exception.cause)
    }

    @Test
    fun `createOrder throws EncodedException when the request fails without a response`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/orders"))
            .andRespond(withException(IOException("Connection reset")))

        // Act
        val exception = assertThrows(EncodedException::class.java) { encodedClient.createOrder(createOrderRequest()) }

        // Assert
        assertNull(exception.httpStatus)
        assertInstanceOf(ResourceAccessException::class.java, exception.cause)
    }

    @Test
    fun `createOrder throws EncodedException when the response has no body`() {
        // Arrange
        mockServer.expect(requestTo("$BASE_URL/orders")).andRespond(withStatus(HttpStatus.CREATED))

        // Act
        val exception = assertThrows(EncodedException::class.java) { encodedClient.createOrder(createOrderRequest()) }

        // Assert
        assertEquals("Encoded create order response had no body", exception.message)
    }

    @ParameterizedTest
    @ValueSource(ints = [0, 2])
    fun `createOrder throws EncodedException when the response does not contain exactly one order`(orderCount: Int) {
        // Arrange
        val responseBody = (1..orderCount).joinToString(",", "[", "]") { """{"object":"order","id":"order-$it"}""" }
        mockServer.expect(requestTo("$BASE_URL/orders")).andRespond(createdResponse(responseBody))

        // Act
        val exception = assertThrows(EncodedException::class.java) { encodedClient.createOrder(createOrderRequest()) }

        // Assert
        assertEquals("Encoded create order response contained $orderCount orders instead of 1", exception.message)
    }

    private fun createOrderRequest() =
        EncodedCreateOrderRequest(
            ref = REFERENCE,
            description = DESCRIPTION,
            currency = "GBP",
            totalAmount = BigDecimal("65.00"),
            attributes =
                EncodedOrderAttributes(
                    govUkPay = EncodedGovUkPayAttributes(paymentId = GOV_UK_PAY_PAYMENT_ID, oneTimeToken = ONE_TIME_TOKEN),
                ),
            paynow = EncodedPayNowOptions(returnUrl = RETURN_URL),
        )

    private fun createdResponse(body: String = CREATED_ORDERS_JSON) =
        withStatus(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(body)

    companion object {
        private const val BASE_URL = "https://encoded.test/api/v1"
        private const val REFERENCE = "f0a3c9d2-6b1e-4c3a-9d6f-2a7b8c9d0e1f"
        private const val DESCRIPTION = "Property registration fee"
        private const val GOV_UK_PAY_PAYMENT_ID = "hu20sqlact5260q2nanm0q8u93"
        private const val ONE_TIME_TOKEN = "gov-uk-pay-one-time-token"
        private const val RETURN_URL = "https://example.com/encoded/return"
        private const val ORDER_ID = "96d7d98a-46fd-4d3b-a15b-b03c116ae949"
        private const val PAYNOW_URL = "https://sit.encoded.services/paynow/$ORDER_ID"
        private const val CREATED_ORDERS_JSON =
            """[{"object":"order","id":"$ORDER_ID","ref":"$REFERENCE","links":{"paynow":"$PAYNOW_URL"}}]"""
    }
}
