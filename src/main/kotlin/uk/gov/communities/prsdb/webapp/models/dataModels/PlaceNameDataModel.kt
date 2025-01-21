package uk.gov.communities.prsdb.webapp.models.dataModels

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PlaceNameDataModel(
    val name: String,
)
