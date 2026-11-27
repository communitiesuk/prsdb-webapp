package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.clients.EncodedClient
import uk.gov.communities.prsdb.webapp.exceptions.EncodedException
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedCreateOrderRequest
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedGovUkPayAttributes
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedOrderAttributes
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedPayNowOptions
import uk.gov.communities.prsdb.webapp.models.dataModels.encoded.EncodedPayNowOrder
import java.math.BigDecimal

@PrsdbWebService
class EncodedOrderService(
    private val encodedClient: EncodedClient,
) {
    fun createPayNowOrder(
        amountInPence: Int,
        reference: String,
        description: String,
        govUkPayPaymentId: String,
        oneTimeToken: String,
        returnUrl: String,
    ): EncodedPayNowOrder {
        require(amountInPence > 0) {
            "Cannot create an Encoded order for GOV.UK Pay payment $govUkPayPaymentId: the amount must be greater than zero " +
                "but was ${amountInPence}p"
        }

        val order =
            encodedClient.createOrder(
                EncodedCreateOrderRequest(
                    ref = reference,
                    description = description,
                    currency = CURRENCY,
                    totalAmount = BigDecimal.valueOf(amountInPence.toLong(), 2),
                    attributes =
                        EncodedOrderAttributes(
                            govUkPay = EncodedGovUkPayAttributes(paymentId = govUkPayPaymentId, oneTimeToken = oneTimeToken),
                        ),
                    paynow = EncodedPayNowOptions(returnUrl = returnUrl),
                ),
            )

        val payNowUrl =
            order.links.paynow?.takeIf { it.isNotBlank() }
                ?: throw EncodedException("Encoded order ${order.id} had no PayNow link")

        return EncodedPayNowOrder(orderId = order.id, payNowUrl = payNowUrl)
    }

    companion object {
        private const val CURRENCY = "GBP"
    }
}
