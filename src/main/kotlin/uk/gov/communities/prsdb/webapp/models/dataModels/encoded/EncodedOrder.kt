package uk.gov.communities.prsdb.webapp.models.dataModels.encoded

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class EncodedOrder(
    val id: String,
    val ref: String? = null,
    val links: EncodedOrderLinks = EncodedOrderLinks(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class EncodedOrderLinks(
    val paynow: String? = null,
)
