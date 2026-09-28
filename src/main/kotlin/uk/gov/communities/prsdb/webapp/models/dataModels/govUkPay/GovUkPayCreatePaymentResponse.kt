package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayCreatePaymentResponse(
    @JsonProperty("payment_id") val paymentId: String? = null,
    @JsonProperty("_links") val links: GovUkPayLinks? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayLinks(
    @JsonProperty("next_url") val nextUrl: GovUkPayLink? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayLink(
    val href: String? = null,
)
