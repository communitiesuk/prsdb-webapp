package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordInvitationAcceptedOtherLandlordEmail(
    val recipientName: String,
    val acceptedLandlordName: String,
    val propertyAddress: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val acceptedLandlordNameKey = "invitee name"
    private val propertyAddressKey = "property address"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_ACCEPTED_OTHER_LANDLORD_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            acceptedLandlordNameKey to acceptedLandlordName,
            propertyAddressKey to propertyAddress,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
