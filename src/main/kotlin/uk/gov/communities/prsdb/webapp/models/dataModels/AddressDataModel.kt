package uk.gov.communities.prsdb.webapp.models.dataModels

data class AddressDataModel(
    val address: String,
    val postcode: String,
    val houseNumber: Int? = null,
    val houseName: String? = null,
    val poBoxNumber: String? = null,
)
