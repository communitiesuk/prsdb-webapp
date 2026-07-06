package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordInvitationRejectionEmail(
    val recipientName: String,
    val inviteeEmail: String,
    val propertyAddress: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val inviteeEmailKey = "invitee email"
    private val propertyAddressKey = "property address"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_REJECTION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            inviteeEmailKey to inviteeEmail,
            propertyAddressKey to propertyAddress,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
