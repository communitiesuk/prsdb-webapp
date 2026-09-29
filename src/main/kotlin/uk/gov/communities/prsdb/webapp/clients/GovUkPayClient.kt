package uk.gov.communities.prsdb.webapp.clients

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.GovUkPayException
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatePaymentResponse
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayCreatedPayment
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayErrorResponse
import uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay.GovUkPayPayment

// API reference: https://docs.payments.service.gov.uk/api_reference/
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
                .toBodilessEntity()
        }
    }

    fun cancelPayment(paymentId: String) {
        sendWithRateLimitRetries {
            client
                .post()
                .uri("/v1/payments/{paymentId}/cancel", paymentId)
                .retrieve()
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
                .body<GovUkPayPayment>()
        } ?: throw GovUkPayException("GOV.UK Pay get payment response for payment $paymentId had no body")

    // GOV.UK Pay rejects rate-limited requests without acting on them, so retrying after a 429 is safe for every endpoint
    // Rate limits: https://docs.payments.service.gov.uk/api_reference/#rate-limits
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
        } catch (exception: RestClientResponseException) {
            val errorResponse = exception.govUkPayErrorResponseOrNull()
            throw GovUkPayException(exception.statusCode, errorResponse?.code, errorResponse?.description)
        } catch (exception: RestClientException) {
            throw GovUkPayException("GOV.UK Pay request failed: ${exception.message}", exception)
        }

    // Error responses from outside GOV.UK Pay (e.g. a gateway error page) may not be JSON
    private fun RestClientResponseException.govUkPayErrorResponseOrNull(): GovUkPayErrorResponse? =
        try {
            getResponseBodyAs(GovUkPayErrorResponse::class.java)
        } catch (exception: RestClientException) {
            null
        }
}
