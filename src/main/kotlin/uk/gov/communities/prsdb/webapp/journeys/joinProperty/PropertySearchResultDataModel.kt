package uk.gov.communities.prsdb.webapp.journeys.joinProperty

import kotlinx.serialization.Serializable

@Serializable
data class PropertySearchResultDataModel(
    val id: Long,
    val address: String,
    val registrationNumber: String,
    val localCouncil: String?,
    val landlordName: String,
)
