package uk.gov.communities.prsdb.webapp.models.dataModels

data class RegisteredPropertyDataModel(
    val address: String,
    val registrationNumber: String,
    val localAuthorityName: String,
    val propertyLicence: String,
    val isTenanted: String,
)
