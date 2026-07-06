package uk.gov.communities.prsdb.webapp.models.dataModels.plausible

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlausibleQueryResponse(
    val results: List<PlausibleResultRow> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlausibleResultRow(
    val metrics: List<Double> = emptyList(),
    val dimensions: List<String> = emptyList(),
)
