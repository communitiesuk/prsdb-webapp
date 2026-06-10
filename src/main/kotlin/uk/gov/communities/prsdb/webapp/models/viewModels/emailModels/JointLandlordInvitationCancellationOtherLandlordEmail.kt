package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordInvitationCancellationOtherLandlordEmail(
    val recipientName: String,
    val invitedEmail: String,
    val propertyAddress: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val invitedEmailKey = "invited joint landlord"
    private val propertyAddressKey = "property address"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_CANCELLATION_OTHER_LANDLORD_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            invitedEmailKey to invitedEmail,
            propertyAddressKey to propertyAddress,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
