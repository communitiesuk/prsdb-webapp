package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

import com.fasterxml.jackson.annotation.JsonProperty

enum class GovUkPayPaymentStatus {
    @JsonProperty("created")
    CREATED,

    @JsonProperty("started")
    STARTED,

    @JsonProperty("submitted")
    SUBMITTED,

    @JsonProperty("capturable")
    CAPTURABLE,

    @JsonProperty("success")
    SUCCESS,

    @JsonProperty("failed")
    FAILED,

    @JsonProperty("cancelled")
    CANCELLED,

    @JsonProperty("error")
    ERROR,
}
