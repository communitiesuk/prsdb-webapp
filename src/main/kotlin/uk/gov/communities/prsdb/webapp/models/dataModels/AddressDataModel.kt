package uk.gov.communities.prsdb.webapp.models.dataModels

data class AddressDataModel(
    val address: String,
    val postcode: String,
    val buildingNumber: Int? = null,
    val buildingName: String? = null,
    val poBoxNumber: String? = null,
)
