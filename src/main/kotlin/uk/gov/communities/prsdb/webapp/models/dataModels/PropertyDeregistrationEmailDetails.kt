package uk.gov.communities.prsdb.webapp.models.dataModels

data class PropertyDeregistrationEmailDetails(
    val landlordRecipients: List<LandlordEmailRecipient>,
    val cancelledInvitationEmailAddresses: List<String>,
    val singleLineAddress: String,
    val multiLineAddress: String,
)
