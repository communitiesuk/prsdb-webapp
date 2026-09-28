package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty

@JsonInclude(JsonInclude.Include.NON_NULL)
data class GovUkPayCreatePaymentRequest(
    val amount: Int,
    val reference: String,
    val description: String,
    @get:JsonProperty("return_url") val returnUrl: String,
    val email: String? = null,
    val language: GovUkPayLanguage? = null,
) {
    // Payments are always deferred so the service can capture or cancel them once it knows the outcome
    @get:JsonProperty("delayed_capture")
    val delayedCapture: Boolean
        get() = true
}
