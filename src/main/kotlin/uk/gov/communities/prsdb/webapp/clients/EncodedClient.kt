package uk.gov.communities.prsdb.webapp.clients

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.exceptions.EncodedException
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedCreateOrderRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrder

/**
 * API reference: Encoded's Gateway API documentation, supplemented by the PayNow Agent Assist Integration Guide.
 */
@PrsdbWebService
class EncodedClient(
    @Qualifier("encoded-client") private val client: RestClient,
) {
    fun createOrder(order: EncodedCreateOrderRequest): EncodedOrder {
        // The create Orders endpoint takes and returns an array, even for a single Order
        val createdOrders =
            send {
                client
                    .post()
                    .uri("/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(listOf(order))
                    .retrieve()
                    .requireStatus(HttpStatus.CREATED, "create order")
                    .body<List<EncodedOrder>>()
            } ?: throw EncodedException("Encoded create order response had no body")

        return createdOrders.singleOrNull()
            ?: throw EncodedException("Encoded create order response contained ${createdOrders.size} orders instead of 1")
    }

    private fun <T> send(request: () -> T): T =
        try {
            request()
        } catch (exception: RestClientResponseException) {
            throw EncodedException(exception.statusCode, exception)
        } catch (exception: RestClientException) {
            throw EncodedException("Encoded request failed: ${exception.message}", exception)
        }

    private fun RestClient.ResponseSpec.requireStatus(
        expectedStatus: HttpStatus,
        operation: String,
    ): RestClient.ResponseSpec =
        onStatus({ !it.isError && it.value() != expectedStatus.value() }) { _, response ->
            throw EncodedException(
                "Encoded responded to $operation with HTTP status ${response.statusCode.value()} instead of ${expectedStatus.value()}",
            )
        }
}
