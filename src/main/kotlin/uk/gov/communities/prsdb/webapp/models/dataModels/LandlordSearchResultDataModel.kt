package uk.gov.communities.prsdb.webapp.models.dataModels

data class LandlordSearchResultDataModel(
    val id: Long,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val registrationNumber: Long,
    val singleLineAddress: String,
    val propertyCount: Long,
)
