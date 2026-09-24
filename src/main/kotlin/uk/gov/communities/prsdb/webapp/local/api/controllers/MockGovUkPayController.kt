package uk.gov.communities.prsdb.webapp.local.api.controllers

import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

/**
 * Mocks the GOV.UK Pay endpoints we use, for local development. Only the standard `web` payment journey is supported.
 *
 * API reference: https://docs.payments.service.gov.uk/api_reference/#api-reference
 *
 * When updating, follow the request parameters and response attributes listed on each endpoint's reference page.
 * Attributes that the real API only returns in circumstances this mock can't produce (e.g. Stripe fees, Worldpay SCA
 * exemptions, recurring payment agreements, corporate card surcharges, 3D Secure, digital wallets) are omitted, as they
 * would be for a real payment made using a test (sandbox) account.
 */
@Profile("local")
@PrsdbRestController
@RequestMapping("/local/gov-uk-pay")
class MockGovUkPayController(
    @Value("\${server.port}") private val serverPort: String,
) {
    private val payments = ConcurrentHashMap<String, StoredPayment>()

    private val baseUrl get() = "http://localhost:$serverPort/local/gov-uk-pay"

    @PostMapping("/v1/payments")
    fun createPayment(
        @RequestBody body: String,
    ): ResponseEntity<String> {
        val request = JSONObject(body)

        val amount = request.optInt("amount", 0)
        val reference = request.optString("reference", "")
        val description = request.optString("description", "")
        val returnUrl = request.optString("return_url", "")

        createPaymentValidationError(amount, reference, description, returnUrl)?.let {
            return jsonResponse(HttpStatus.UNPROCESSABLE_ENTITY, it)
        }

        val prefilledDetails = request.optJSONObject("prefilled_cardholder_details")
        val payment =
            StoredPayment(
                paymentId = generatePaymentId(),
                amount = amount,
                reference = reference,
                description = description,
                returnUrl = returnUrl,
                language = request.optString("language", "en"),
                email = request.optStringOrNull("email"),
                delayedCapture = request.optBoolean("delayed_capture", false),
                moto = request.optBoolean("moto", false),
                authorisationMode = request.optString("authorisation_mode", "web"),
                metadata = request.optJSONObject("metadata"),
                cardholderName = prefilledDetails?.optStringOrNull("cardholder_name"),
                billingAddress = prefilledDetails?.optJSONObject("billing_address"),
                providerId = Random.nextLong(1_000_000_000L, 9_999_999_999L).toString(),
                chargeToken = UUID.randomUUID().toString(),
                createdDate = Instant.now().toString(),
            )
        payments[payment.paymentId] = payment
        return jsonResponse(HttpStatus.CREATED, payment.toResponseJson())
    }

    // Stands in for GOV.UK Pay's hosted card page: handed to the client as next_url, it "completes" card entry and
    // redirects back to the return_url, moving the payment to capturable (delayed capture) or success.
    @GetMapping("/mock-card-page/{paymentId}")
    fun mockCardPage(
        @PathVariable paymentId: String,
    ): ResponseEntity<String> {
        val payment = payments[paymentId] ?: return notFound()
        if (payment.delayedCapture) {
            payment.markCapturable()
        } else {
            payment.markCaptured()
        }
        val redirect = payment.returnUrl.ifBlank { "$baseUrl/v1/payments/$paymentId" }
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(redirect)).build()
    }

    @GetMapping("/v1/payments/{paymentId}")
    fun getPayment(
        @PathVariable paymentId: String,
    ): ResponseEntity<String> {
        val payment = payments[paymentId] ?: return notFound()
        return jsonResponse(HttpStatus.OK, payment.toResponseJson())
    }

    @PostMapping("/v1/payments/{paymentId}/capture")
    fun capturePayment(
        @PathVariable paymentId: String,
    ): ResponseEntity<String> {
        val payment = payments[paymentId] ?: return notFound()
        if (payment.status != "capturable") {
            return jsonResponse(
                HttpStatus.BAD_REQUEST,
                errorJson("P0104", "Payment cannot be captured because it is not in a capturable state"),
            )
        }
        payment.markCaptured()
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/v1/payments/{paymentId}/cancel")
    fun cancelPayment(
        @PathVariable paymentId: String,
    ): ResponseEntity<String> {
        val payment = payments[paymentId] ?: return notFound()
        if (payment.finished) {
            return jsonResponse(HttpStatus.BAD_REQUEST, errorJson("P0501", "Cancellation of payment failed"))
        }
        payment.markCancelled()
        return ResponseEntity.noContent().build()
    }

    private fun createPaymentValidationError(
        amount: Int,
        reference: String,
        description: String,
        returnUrl: String,
    ): String? =
        when {
            amount <= 0 -> errorJson("P0102", "Invalid attribute value: amount. Must be a positive integer in pence")
            reference.isBlank() -> errorJson("P0101", "Missing mandatory attribute: reference")
            description.isBlank() -> errorJson("P0101", "Missing mandatory attribute: description")
            returnUrl.isBlank() -> errorJson("P0101", "Missing mandatory attribute: return_url")
            else -> null
        }

    private fun StoredPayment.toResponseJson(): String {
        val conditionalFields =
            buildString {
                email?.let { append(""","email":${JSONObject.quote(it)}""") }
                metadata?.let { append(""","metadata":$it""") }
                if (cardEntered) {
                    append(""","provider_id":"$providerId"""")
                    append(""","card_details":${cardDetailsJson()}""")
                }
            }
        return """
            {
                "amount": $amount,
                "description": ${JSONObject.quote(description)},
                "reference": ${JSONObject.quote(reference)},
                "language": ${JSONObject.quote(language)},
                "state": ${stateJson()},
                "payment_id": "$paymentId",
                "payment_provider": "sandbox",
                "created_date": "$createdDate",
                "refund_summary": ${refundSummaryJson()},
                "settlement_summary": ${settlementSummaryJson()},
                "delayed_capture": $delayedCapture,
                "moto": $moto,
                "return_url": ${JSONObject.quote(returnUrl)},
                "authorisation_mode": ${JSONObject.quote(authorisationMode)},
                "_links": ${linksJson()}$conditionalFields
            }
            """.trimIndent()
    }

    private fun StoredPayment.stateJson(): String =
        if (status == "cancelled") {
            """{ "status": "$status", "finished": $finished, "message": "Payment was cancelled by your service", "code": "P0040" }"""
        } else {
            """{ "status": "$status", "finished": $finished }"""
        }

    private fun StoredPayment.refundSummaryJson(): String {
        val (refundStatus, amountAvailable) =
            when (status) {
                "success" -> "available" to amount
                "cancelled", "failed", "error" -> "unavailable" to 0
                else -> "pending" to amount
            }
        return """{ "status": "$refundStatus", "amount_available": $amountAvailable, "amount_submitted": 0 }"""
    }

    private fun StoredPayment.settlementSummaryJson(): String =
        capturedAt?.let {
            """{ "capture_submit_time": "$it", "captured_date": "${LocalDate.ofInstant(it, ZoneOffset.UTC)}" }"""
        } ?: "{}"

    private fun StoredPayment.cardDetailsJson(): String {
        val billing =
            billingAddress?.toString()
                ?: """{ "line1": "10 Downing Street", "postcode": "SW1A 2AA", "city": "London", "country": "GB" }"""
        return """
            {
                "card_brand": "Visa",
                "card_type": "debit",
                "last_digits_card_number": "1234",
                "first_digits_card_number": "424242",
                "expiry_date": "12/30",
                "cardholder_name": ${JSONObject.quote(cardholderName ?: "Sherlock Holmes")},
                "billing_address": $billing
            }
            """.trimIndent()
    }

    private fun StoredPayment.linksJson(): String {
        val payment = "$baseUrl/v1/payments/$paymentId"
        val cardPage = "$baseUrl/mock-card-page/$paymentId"
        val stateLinks =
            when (status) {
                "created" ->
                    """
                    ,"next_url": { "href": "$cardPage", "method": "GET" }
                    ,"next_url_post": { "type": "application/x-www-form-urlencoded", "params": { "chargeTokenId": "$chargeToken" }, "href": "$cardPage", "method": "POST" }
                    ,"cancel": { "href": "$payment/cancel", "method": "POST" }
                    """.trimIndent()

                "capturable" ->
                    """
                    ,"capture": { "href": "$payment/capture", "method": "POST" }
                    ,"cancel": { "href": "$payment/cancel", "method": "POST" }
                    """.trimIndent()

                else -> ""
            }
        return """
            {
                "self": { "href": "$payment", "method": "GET" },
                "events": { "href": "$payment/events", "method": "GET" },
                "refunds": { "href": "$payment/refunds", "method": "GET" }$stateLinks
            }
            """.trimIndent()
    }

    private fun errorJson(
        code: String,
        description: String,
    ): String = """{ "code": "$code", "description": ${JSONObject.quote(description)} }"""

    private fun notFound(): ResponseEntity<String> = jsonResponse(HttpStatus.NOT_FOUND, errorJson("P0200", "Not found"))

    private fun jsonResponse(
        status: HttpStatus,
        body: String,
    ): ResponseEntity<String> = ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(body)

    private fun JSONObject.optStringOrNull(key: String): String? = if (has(key) && !isNull(key)) getString(key) else null

    private fun generatePaymentId(): String {
        val chars = "abcdefghijklmnopqrstuvwxyz0123456789"
        return (1..26).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private class StoredPayment(
        val paymentId: String,
        val amount: Int,
        val reference: String,
        val description: String,
        val returnUrl: String,
        val language: String,
        val email: String?,
        val delayedCapture: Boolean,
        val moto: Boolean,
        val authorisationMode: String,
        val metadata: JSONObject?,
        val cardholderName: String?,
        val billingAddress: JSONObject?,
        val providerId: String,
        val chargeToken: String,
        val createdDate: String,
    ) {
        var status: String = "created"
            private set

        var cardEntered: Boolean = false
            private set

        var capturedAt: Instant? = null
            private set

        val finished: Boolean
            get() = status in setOf("success", "cancelled", "failed", "error")

        fun markCapturable() {
            status = "capturable"
            cardEntered = true
        }

        fun markCaptured() {
            status = "success"
            cardEntered = true
            capturedAt = Instant.now()
        }

        fun markCancelled() {
            status = "cancelled"
        }
    }
}
