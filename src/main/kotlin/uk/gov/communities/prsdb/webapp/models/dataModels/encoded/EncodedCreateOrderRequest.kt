package uk.gov.communities.prsdb.webapp.models.dataModels.encoded

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class EncodedCreateOrderRequest(
    val ref: String,
    val description: String,
    val currency: String,
    // Major currency units, e.g. 65.00 for £65
    val totalAmount: BigDecimal,
    val attributes: EncodedOrderAttributes,
    val paynow: EncodedPayNowOptions,
) {
    @get:JsonProperty("object")
    val objectType: String
        get() = "order"
}

data class EncodedOrderAttributes(
    val govUkPay: EncodedGovUkPayAttributes,
)

data class EncodedGovUkPayAttributes(
    val paymentId: String,
    val oneTimeToken: String,
)

data class EncodedPayNowOptions(
    val returnUrl: String,
)
