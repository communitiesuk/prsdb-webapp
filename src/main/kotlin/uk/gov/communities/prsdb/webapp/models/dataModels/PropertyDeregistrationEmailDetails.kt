package uk.gov.communities.prsdb.webapp.models.dataModels

data class PropertyDeregistrationEmailDetails(
    val landlordEmailAddresses: List<String>,
    val prn: String,
    val singleLineAddress: String,
)
