package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

import com.fasterxml.jackson.annotation.JsonProperty

// https://docs.payments.service.gov.uk/moto_payments/moto_send_card_details_api/
data class GovUkPayCreateMotoPaymentRequest(
    val amount: Int,
    val reference: String,
    val description: String,
) {
    // No GOV.UK Pay page is shown: Encoded collects the card details and sends them, with the one-time token,
    // to the auth_url_post link (POST /v1/auth)
    @get:JsonProperty("authorisation_mode")
    val authorisationMode: String
        get() = "moto_api"

    // Payments are always deferred so the service can capture or cancel them once it knows the outcome
    @get:JsonProperty("delayed_capture")
    val delayedCapture: Boolean
        get() = true
}
