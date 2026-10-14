package uk.gov.communities.prsdb.webapp.models.dataModels.govUkPay

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class GovUkPayErrorResponse(
    val code: String? = null,
    val description: String? = null,
)
