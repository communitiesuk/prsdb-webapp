package uk.gov.communities.prsdb.webapp.clients

import org.hamcrest.Matchers.startsWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.json.JsonCompareMode
import org.springframework.test.web.client.ExpectedCount
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.header
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayLink
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentLinks
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentState
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPaymentStatus
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPaySettlementSummary
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

class GovUkPayClientTests {
    private lateinit var mockServer: MockRestServiceServer
    private lateinit var restClient: RestClient
    private lateinit var govUkPayClient: GovUkPayClient

    @BeforeEach
    fun setUp() {
        val builder = RestClient.builder().baseUrl(BASE_URL)
        mockServer = MockRestServiceServer.bindTo(builder).build()
        restClient = builder.build()
        govUkPayClient = GovUkPayClient(restClient, RATE_LIMIT_RETRY_DELAYS_MS)
    }

    @Test
    fun `createPayment posts a deferred payment request with all provided fields`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.CONTENT_TYPE, startsWith(MediaType.APPLICATION_JSON_VALUE)))
            .andExpect(
                content().json(
                    """
                    {
                        "amount": $AMOUNT,
                        "reference": "$REFERENCE",
                        "description": "$DESCRIPTION",
                        "return_url": "$RETURN_URL",
                        "email": "$EMAIL",
                        "delayed_capture": true
                    }
                    """,
                    JsonCompareMode.STRICT,
                ),
            ).andRespond(createdResponse())

        // Act
        govUkPayClient.createPayment(createPaymentRequest(email = EMAIL))

        // Assert
        mockServer.verify()
    }

    @Test
    fun `createPayment omits optional fields that are not provided`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments"))
            .andExpect(
                content().json(
                    """
                    {
                        "amount": $AMOUNT,
                        "reference": "$REFERENCE",
                        "description": "$DESCRIPTION",
                        "return_url": "$RETURN_URL",
                        "delayed_capture": true
                    }
                    """,
                    JsonCompareMode.STRICT,
                ),
            ).andRespond(createdResponse())

        // Act
        govUkPayClient.createPayment(createPaymentRequest())

        // Assert
        mockServer.verify()
    }

    @Test
    fun `createPayment returns the created payment and its next URL`() {
        // Arrange
        mockServer.expect(requestTo("$BASE_URL/v1/payments")).andRespond(createdResponse())

        // Act
        val createdPayment = govUkPayClient.createPayment(createPaymentRequest())

        // Assert
        assertEquals(expectedCreatedPayment(), createdPayment)
        mockServer.verify()
    }

    @Test
    fun `createPayment throws GovUkPayException with the error details from an error response`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments"))
            .andRespond(errorResponse(HttpStatus.UNPROCESSABLE_ENTITY, "P0102", "Invalid attribute value: amount"))

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { govUkPayClient.createPayment(createPaymentRequest()) }

        // Assert
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), exception.httpStatus?.value())
        assertEquals("P0102", exception.errorCode)
        assertEquals("Invalid attribute value: amount", exception.errorDescription)
        assertEquals("GOV.UK Pay request failed with HTTP status 422: P0102 - Invalid attribute value: amount", exception.message)
        assertInstanceOf(RestClientResponseException::class.java, exception.cause)
        mockServer.verify()
    }

    @Test
    fun `createPayment throws GovUkPayException when the created payment has no next URL`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments"))
            .andRespond(createdResponse(includeNextUrl = false))

        // Act, Assert
        assertThrows(GovUkPayException::class.java) { govUkPayClient.createPayment(createPaymentRequest()) }
        mockServer.verify()
    }

    @Test
    fun `createPayment wraps a network failure in GovUkPayException`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments"))
            .andRespond { throw IOException("Connection refused") }

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { govUkPayClient.createPayment(createPaymentRequest()) }

        // Assert
        assertNull(exception.httpStatus)
        assertInstanceOf(ResourceAccessException::class.java, exception.cause)
        mockServer.verify()
    }

    @Test
    fun `capturePayment posts to the capture endpoint and succeeds on a 204 response`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID/capture"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withNoContent())

        // Act
        govUkPayClient.capturePayment(PAYMENT_ID)

        // Assert
        mockServer.verify()
    }

    @ParameterizedTest
    @MethodSource("provideCaptureErrorResponses")
    fun `capturePayment throws GovUkPayException with the error details from an error response`(
        status: HttpStatus,
        code: String,
    ) {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID/capture"))
            .andRespond(errorResponse(status, code, "Capture failed"))

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { govUkPayClient.capturePayment(PAYMENT_ID) }

        // Assert
        assertEquals(status.value(), exception.httpStatus?.value())
        assertEquals(code, exception.errorCode)
        mockServer.verify()
    }

    @Test
    fun `capturePayment throws GovUkPayException without error details for a non-JSON error response and does not retry`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID/capture"))
            .andRespond(
                withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Internal Server Error"),
            )

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { govUkPayClient.capturePayment(PAYMENT_ID) }

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), exception.httpStatus?.value())
        assertNull(exception.errorCode)
        assertNull(exception.errorDescription)
        assertEquals("GOV.UK Pay request failed with HTTP status 500", exception.message)
        val cause = assertInstanceOf(RestClientResponseException::class.java, exception.cause)
        assertEquals("Internal Server Error", cause.responseBodyAsString)
        mockServer.verify()
    }

    @Test
    fun `cancelPayment posts to the cancel endpoint and succeeds on a 204 response`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID/cancel"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(withNoContent())

        // Act
        govUkPayClient.cancelPayment(PAYMENT_ID)

        // Assert
        mockServer.verify()
    }

    @ParameterizedTest
    @MethodSource("provideCancelErrorResponses")
    fun `cancelPayment throws GovUkPayException with the error details from an error response`(
        status: HttpStatus,
        code: String,
    ) {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID/cancel"))
            .andRespond(errorResponse(status, code, "Cancellation failed"))

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { govUkPayClient.cancelPayment(PAYMENT_ID) }

        // Assert
        assertEquals(status.value(), exception.httpStatus?.value())
        assertEquals(code, exception.errorCode)
        mockServer.verify()
    }

    @Test
    fun `getPayment returns the parsed payment details`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(paymentBody(), MediaType.APPLICATION_JSON))

        // Act
        val payment = govUkPayClient.getPayment(PAYMENT_ID)

        // Assert
        assertEquals(
            GovUkPayPayment(
                paymentId = PAYMENT_ID,
                amount = AMOUNT,
                reference = REFERENCE,
                description = DESCRIPTION,
                email = EMAIL,
                createdDate = Instant.parse(CREATED_DATE),
                state = GovUkPayPaymentState(status = GovUkPayPaymentStatus.SUCCESS, finished = true),
                settlementSummary = GovUkPaySettlementSummary(capturedDate = LocalDate.parse(CAPTURED_DATE)),
            ),
            payment,
        )
        mockServer.verify()
    }

    @Test
    fun `getPayment returns the failure code and message and no captured date for a failed payment`() {
        // Arrange
        val failedPaymentBody =
            paymentBody(
                stateJson = """{ "status": "failed", "finished": true, "code": "P0010", "message": "Payment method rejected" }""",
                settlementSummaryJson = "{}",
            )
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(withSuccess(failedPaymentBody, MediaType.APPLICATION_JSON))

        // Act
        val payment = govUkPayClient.getPayment(PAYMENT_ID)

        // Assert
        assertEquals(
            GovUkPayPaymentState(
                status = GovUkPayPaymentStatus.FAILED,
                finished = true,
                code = "P0010",
                message = "Payment method rejected",
            ),
            payment.state,
        )
        assertNull(payment.settlementSummary?.capturedDate)
        mockServer.verify()
    }

    @Test
    fun `getPayment returns the cancel link for a payment that can be cancelled`() {
        // Arrange
        val cancelUrl = "$BASE_URL/v1/payments/$PAYMENT_ID/cancel"
        val cancellablePaymentBody =
            paymentBody(
                stateJson = """{ "status": "started", "finished": false }""",
                settlementSummaryJson = "{}",
                linksJson =
                    """
                    {
                        "self": { "href": "$BASE_URL/v1/payments/$PAYMENT_ID", "method": "GET" },
                        "cancel": { "href": "$cancelUrl", "method": "POST" }
                    }
                    """,
            )
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(withSuccess(cancellablePaymentBody, MediaType.APPLICATION_JSON))

        // Act
        val payment = govUkPayClient.getPayment(PAYMENT_ID)

        // Assert
        assertEquals(GovUkPayLink(href = cancelUrl), payment.links.cancel)
        mockServer.verify()
    }

    @Test
    fun `getPayment returns no cancel link for a payment that cannot be cancelled`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(withSuccess(paymentBody(), MediaType.APPLICATION_JSON))

        // Act
        val payment = govUkPayClient.getPayment(PAYMENT_ID)

        // Assert
        assertNull(payment.links.cancel)
        mockServer.verify()
    }

    @Test
    fun `getPayment throws GovUkPayException for an unrecognised payment status`() {
        // Arrange
        val unknownStatusBody = paymentBody(stateJson = """{ "status": "some_new_status", "finished": false }""")
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(withSuccess(unknownStatusBody, MediaType.APPLICATION_JSON))

        // Act, Assert
        assertThrows(GovUkPayException::class.java) { govUkPayClient.getPayment(PAYMENT_ID) }
        mockServer.verify()
    }

    @Test
    fun `getPayment throws GovUkPayException with the error details for a 404 response`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(errorResponse(HttpStatus.NOT_FOUND, "P0200", "Not found"))

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { govUkPayClient.getPayment(PAYMENT_ID) }

        // Assert
        assertEquals(HttpStatus.NOT_FOUND.value(), exception.httpStatus?.value())
        assertEquals("P0200", exception.errorCode)
        mockServer.verify()
    }

    @Test
    fun `createPayment retries after a 429 response and returns the created payment`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments"))
            .andRespond(errorResponse(HttpStatus.TOO_MANY_REQUESTS, "P0900", "Too many requests"))
        mockServer.expect(requestTo("$BASE_URL/v1/payments")).andRespond(createdResponse())

        // Act
        val createdPayment = govUkPayClient.createPayment(createPaymentRequest())

        // Assert
        assertEquals(expectedCreatedPayment(), createdPayment)
        mockServer.verify()
    }

    @Test
    fun `capturePayment retries after a 429 response and succeeds on a 204 response`() {
        // Arrange
        mockServer
            .expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID/capture"))
            .andRespond(errorResponse(HttpStatus.TOO_MANY_REQUESTS, "P0900", "Too many requests"))
        mockServer.expect(requestTo("$BASE_URL/v1/payments/$PAYMENT_ID/capture")).andRespond(withNoContent())

        // Act
        govUkPayClient.capturePayment(PAYMENT_ID)

        // Assert
        mockServer.verify()
    }

    @Test
    fun `getPayment throws GovUkPayException once the configured rate limit retries are exhausted`() {
        // Arrange
        mockServer
            .expect(ExpectedCount.times(RATE_LIMIT_RETRY_DELAYS_MS.size + 1), requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(errorResponse(HttpStatus.TOO_MANY_REQUESTS, "P0900", "Too many requests"))

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { govUkPayClient.getPayment(PAYMENT_ID) }

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), exception.httpStatus?.value())
        assertEquals("P0900", exception.errorCode)
        mockServer.verify()
    }

    @Test
    fun `getPayment does not retry a 429 response when no rate limit retry delays are configured`() {
        // Arrange
        val clientWithoutRetries = GovUkPayClient(restClient, emptyList())
        mockServer
            .expect(ExpectedCount.once(), requestTo("$BASE_URL/v1/payments/$PAYMENT_ID"))
            .andRespond(errorResponse(HttpStatus.TOO_MANY_REQUESTS, "P0900", "Too many requests"))

        // Act
        val exception = assertThrows(GovUkPayException::class.java) { clientWithoutRetries.getPayment(PAYMENT_ID) }

        // Assert
        assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), exception.httpStatus?.value())
        mockServer.verify()
    }

    private fun createPaymentRequest(email: String? = null) =
        GovUkPayCreatePaymentRequest(
            amount = AMOUNT,
            reference = REFERENCE,
            description = DESCRIPTION,
            returnUrl = RETURN_URL,
            email = email,
        )

    private fun expectedCreatedPayment() =
        GovUkPayCreatedPayment(
            payment =
                GovUkPayPayment(
                    paymentId = PAYMENT_ID,
                    amount = AMOUNT,
                    reference = REFERENCE,
                    description = DESCRIPTION,
                    createdDate = Instant.parse(CREATED_DATE),
                    state = GovUkPayPaymentState(status = GovUkPayPaymentStatus.CREATED, finished = false),
                    links = GovUkPayPaymentLinks(nextUrl = GovUkPayLink(href = NEXT_URL)),
                ),
            nextUrl = NEXT_URL,
        )

    private fun createdResponse(includeNextUrl: Boolean = true) =
        withStatus(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(createdPaymentBody(includeNextUrl))

    private fun createdPaymentBody(includeNextUrl: Boolean): String {
        val nextUrlLink = if (includeNextUrl) "\"next_url\": { \"href\": \"$NEXT_URL\", \"method\": \"GET\" }," else ""
        return """
            {
                "amount": $AMOUNT,
                "description": "$DESCRIPTION",
                "reference": "$REFERENCE",
                "language": "en",
                "state": { "status": "created", "finished": false },
                "payment_id": "$PAYMENT_ID",
                "payment_provider": "sandbox",
                "created_date": "$CREATED_DATE",
                "delayed_capture": true,
                "moto": false,
                "return_url": "$RETURN_URL",
                "_links": {
                    $nextUrlLink
                    "self": { "href": "$BASE_URL/v1/payments/$PAYMENT_ID", "method": "GET" }
                }
            }
            """
    }

    private fun paymentBody(
        stateJson: String = """{ "status": "success", "finished": true }""",
        settlementSummaryJson: String =
            """{ "capture_submit_time": "2026-09-28T13:15:00.000Z", "captured_date": "$CAPTURED_DATE" }""",
        linksJson: String = """{ "self": { "href": "$BASE_URL/v1/payments/$PAYMENT_ID", "method": "GET" } }""",
    ) = """
        {
            "amount": $AMOUNT,
            "description": "$DESCRIPTION",
            "reference": "$REFERENCE",
            "language": "en",
            "email": "$EMAIL",
            "state": $stateJson,
            "payment_id": "$PAYMENT_ID",
            "payment_provider": "sandbox",
            "created_date": "$CREATED_DATE",
            "refund_summary": { "status": "available", "amount_available": $AMOUNT, "amount_submitted": 0 },
            "settlement_summary": $settlementSummaryJson,
            "delayed_capture": true,
            "moto": false,
            "return_url": "$RETURN_URL",
            "authorisation_mode": "web",
            "_links": $linksJson
        }
        """

    private fun errorResponse(
        status: HttpStatus,
        code: String,
        description: String,
    ) = withStatus(status)
        .contentType(MediaType.APPLICATION_JSON)
        .body("""{ "code": "$code", "description": "$description" }""")

    companion object {
        @JvmStatic
        fun provideCaptureErrorResponses() =
            listOf(
                Arguments.of(HttpStatus.BAD_REQUEST, "P1001"),
                Arguments.of(HttpStatus.NOT_FOUND, "P1000"),
                Arguments.of(HttpStatus.CONFLICT, "P1003"),
            )

        @JvmStatic
        fun provideCancelErrorResponses() =
            listOf(
                Arguments.of(HttpStatus.BAD_REQUEST, "P0501"),
                Arguments.of(HttpStatus.NOT_FOUND, "P0500"),
                Arguments.of(HttpStatus.CONFLICT, "P0502"),
            )

        // Zero delays keep the tests fast; the number of delays is the number of times a 429 response is retried
        private val RATE_LIMIT_RETRY_DELAYS_MS = listOf(0L, 0L)

        private const val BASE_URL = "https://gov-uk-pay.test"
        private const val PAYMENT_ID = "hu20sqlact5260q2nanm0q8u93"
        private const val NEXT_URL = "https://www.payments.service.gov.uk/secure/ef1b6ff1-db34-4c62-b854-3ed4ba3c4049"
        private const val AMOUNT = 2000
        private const val REFERENCE = "P-CCCT-CCCT"
        private const val DESCRIPTION = "Register a rental property"
        private const val RETURN_URL = "https://prsdb.test/landlord/register-property/payment-return"
        private const val EMAIL = "landlord@example.com"
        private const val CREATED_DATE = "2026-09-28T13:11:29.019Z"
        private const val CAPTURED_DATE = "2026-09-28"
    }
}
