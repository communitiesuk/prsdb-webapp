package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant
import java.time.LocalDate

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayPayment(
    @JsonProperty("payment_id") val paymentId: String,
    val amount: Int,
    val reference: String,
    val description: String,
    val email: String? = null,
    @JsonProperty("created_date") val createdDate: Instant,
    val state: GovUkPayPaymentState,
    @JsonProperty("settlement_summary") val settlementSummary: GovUkPaySettlementSummary? = null,
    @JsonProperty("_links") val links: GovUkPayPaymentLinks = GovUkPayPaymentLinks(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayPaymentLinks(
    @JsonProperty("next_url") val nextUrl: GovUkPayLink? = null,
    val cancel: GovUkPayLink? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayLink(
    val href: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayPaymentState(
    val status: GovUkPayPaymentStatus,
    val finished: Boolean,
    val code: String? = null,
    val message: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPaySettlementSummary(
    @JsonProperty("captured_date") val capturedDate: LocalDate? = null,
)
