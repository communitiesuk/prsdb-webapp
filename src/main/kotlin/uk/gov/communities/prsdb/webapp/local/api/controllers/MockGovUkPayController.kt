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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

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

        val amount = if (request.has("amount")) request.optInt("amount", 0) else 0
        val reference = request.optString("reference", "")
        val description = request.optString("description", "")
        val returnUrl = request.optString("return_url", "")

        validationError(amount, reference, description, returnUrl)?.let {
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
                email = request.optStringOrNull("email"),
                delayedCapture = request.optBoolean("delayed_capture", false),
                metadata = request.optJSONObject("metadata"),
                cardholderName = prefilledDetails?.optStringOrNull("cardholder_name"),
                billingAddress = prefilledDetails?.optJSONObject("billing_address"),
                providerId = Random.nextLong(1_000_000_000L, 9_999_999_999L).toString(),
                chargeToken = UUID.randomUUID().toString(),
                createdDate = Instant.now().toString(),
                status = "created",
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
        payment.cardEntered = true
        if (payment.delayedCapture) {
            payment.status = "capturable"
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
        payment.status = "cancelled"
        return ResponseEntity.noContent().build()
    }

    private fun validationError(
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
        val response =
            JSONObject()
                .put("amount", amount)
                .put("description", description)
                .put("reference", reference)
                .put("state", JSONObject().put("status", status).put("finished", finished))
                .put("payment_id", paymentId)
                .put("payment_provider", "sandbox")
                .put("created_date", createdDate)
                .put("delayed_capture", delayedCapture)
                .put("return_url", returnUrl)
                .put("_links", linksJson())

        email?.let { response.put("email", it) }
        metadata?.let { response.put("metadata", it) }
        if (cardEntered) {
            response.put("provider_id", providerId)
            response.put("authorisation_mode", "web")
            response.put("card_details", cardDetailsJson())
        }
        return response.toString()
    }

    private fun StoredPayment.cardDetailsJson(): JSONObject {
        val billing =
            billingAddress ?: JSONObject()
                .put("line1", "10 Downing Street")
                .put("postcode", "SW1A 2AA")
                .put("city", "London")
                .put("country", "GB")
        return JSONObject()
            .put("card_brand", "Visa")
            .put("card_type", "debit")
            .put("last_digits_card_number", "1234")
            .put("first_digits_card_number", "424242")
            .put("expiry_date", "12/30")
            .put("cardholder_name", cardholderName ?: "Sherlock Holmes")
            .put("billing_address", billing)
    }

    private fun StoredPayment.linksJson(): JSONObject {
        val links =
            JSONObject()
                .put("self", link("$baseUrl/v1/payments/$paymentId", "GET"))
                .put("events", link("$baseUrl/v1/payments/$paymentId/events", "GET"))
                .put("refunds", link("$baseUrl/v1/payments/$paymentId/refunds", "GET"))
        when (status) {
            "created" -> {
                links.put("next_url", link("$baseUrl/mock-card-page/$paymentId", "GET"))
                links.put(
                    "next_url_post",
                    JSONObject()
                        .put("type", "application/x-www-form-urlencoded")
                        .put("params", JSONObject().put("chargeTokenId", chargeToken))
                        .put("href", "$baseUrl/mock-card-page/$paymentId")
                        .put("method", "POST"),
                )
                links.put("cancel", link("$baseUrl/v1/payments/$paymentId/cancel", "POST"))
            }

            "capturable" -> {
                links.put("capture", link("$baseUrl/v1/payments/$paymentId/capture", "POST"))
                links.put("cancel", link("$baseUrl/v1/payments/$paymentId/cancel", "POST"))
            }
        }
        return links
    }

    private fun link(
        href: String,
        method: String,
    ): JSONObject = JSONObject().put("href", href).put("method", method)

    private fun errorJson(
        code: String,
        description: String,
    ): String = JSONObject().put("code", code).put("description", description).toString()

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
        val email: String?,
        val delayedCapture: Boolean,
        val metadata: JSONObject?,
        val cardholderName: String?,
        val billingAddress: JSONObject?,
        val providerId: String,
        val chargeToken: String,
        val createdDate: String,
        var status: String,
    ) {
        var cardEntered: Boolean = false

        val finished: Boolean
            get() = status in setOf("success", "cancelled", "failed", "error")

        fun markCaptured() {
            status = "success"
            cardEntered = true
        }
    }
}
