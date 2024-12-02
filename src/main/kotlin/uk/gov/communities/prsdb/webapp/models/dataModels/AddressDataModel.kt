package uk.gov.communities.prsdb.webapp.models.dataModels

data class AddressDataModel(
    val singleLineAddress: String,
    val custodianCode: String,
    val uprn: Int? = null,
    val organisation: String? = null,
    val subBuilding: String? = null,
    val buildingName: String? = null,
    val buildingNumber: String? = null,
    val streetName: String? = null,
    val locality: String? = null,
    val townName: String? = null,
    val postcode: String? = null,
)
