package uk.gov.communities.prsdb.webapp.clients

import org.json.JSONException
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.body
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentResponse
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment

@PrsdbWebService
class GovUkPayClient(
    @Qualifier("gov-uk-pay-client") private val client: RestClient,
    @Value("\${gov-uk-pay.rate-limit-retry-delays-ms:1000,2000}") private val rateLimitRetryDelaysMs: List<Long>,
) {
    fun createPayment(request: GovUkPayCreatePaymentRequest): GovUkPayCreatedPayment {
        val response =
            sendWithRateLimitRetries {
                client
                    .post()
                    .uri("/v1/payments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .throwGovUkPayExceptionOnError()
                    .body<GovUkPayCreatePaymentResponse>()
            }

        val paymentId =
            response?.paymentId
                ?: throw GovUkPayException("GOV.UK Pay create payment response did not include a payment_id")
        val nextUrl =
            response?.links?.nextUrl?.href
                ?: throw GovUkPayException("GOV.UK Pay create payment response for payment $paymentId did not include a next_url")
        return GovUkPayCreatedPayment(paymentId = paymentId, nextUrl = nextUrl)
    }

    fun capturePayment(paymentId: String) {
        sendWithRateLimitRetries {
            client
                .post()
                .uri("/v1/payments/{paymentId}/capture", paymentId)
                .retrieve()
                .throwGovUkPayExceptionOnError()
                .toBodilessEntity()
        }
    }

    fun cancelPayment(paymentId: String) {
        sendWithRateLimitRetries {
            client
                .post()
                .uri("/v1/payments/{paymentId}/cancel", paymentId)
                .retrieve()
                .throwGovUkPayExceptionOnError()
                .toBodilessEntity()
        }
    }

    fun getPayment(paymentId: String): GovUkPayPayment =
        sendWithRateLimitRetries {
            client
                .get()
                .uri("/v1/payments/{paymentId}", paymentId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .throwGovUkPayExceptionOnError()
                .body<GovUkPayPayment>()
        } ?: throw GovUkPayException("GOV.UK Pay get payment response for payment $paymentId had no body")

    // GOV.UK Pay rejects rate-limited requests without acting on them, so retrying after a 429 is safe for every endpoint
    private fun <T> sendWithRateLimitRetries(request: () -> T): T {
        for (delayMs in rateLimitRetryDelaysMs) {
            try {
                return send(request)
            } catch (exception: GovUkPayException) {
                if (exception.httpStatus?.value() != HttpStatus.TOO_MANY_REQUESTS.value()) throw exception
            }
            waitBeforeRetrying(delayMs)
        }
        return send(request)
    }

    private fun waitBeforeRetrying(delayMs: Long) {
        try {
            Thread.sleep(delayMs)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw GovUkPayException("Interrupted while waiting to retry a rate-limited GOV.UK Pay request", exception)
        }
    }

    private fun <T> send(request: () -> T): T =
        try {
            request()
        } catch (exception: RestClientException) {
            throw GovUkPayException("GOV.UK Pay request failed: ${exception.message}", exception)
        }

    private fun RestClient.ResponseSpec.throwGovUkPayExceptionOnError(): RestClient.ResponseSpec =
        onStatus({ it.isError }) { _, response -> throw toGovUkPayException(response) }

    private fun toGovUkPayException(response: ClientHttpResponse): GovUkPayException {
        val errorBody = parseJsonOrNull(response.body.readAllBytes().toString(Charsets.UTF_8))
        return GovUkPayException(
            httpStatus = response.statusCode,
            errorCode = errorBody?.optString("code", null),
            errorDescription = errorBody?.optString("description", null),
        )
    }

    private fun parseJsonOrNull(body: String): JSONObject? =
        try {
            JSONObject(body)
        } catch (exception: JSONException) {
            null
        }
}
