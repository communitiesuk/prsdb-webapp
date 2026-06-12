package uk.gov.communities.prsdb.webapp.models.dataModels.plausible

import com.fasterxml.jackson.annotation.JsonProperty

data class PlausibleQuery(
    @get:JsonProperty("site_id") val siteId: String,
    @get:JsonProperty("date_range") val dateRange: List<String>,
    val metrics: List<String>,
    val dimensions: List<String>,
    val filters: List<List<Any>>,
)
