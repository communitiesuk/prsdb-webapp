package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordInvitationNotifyExistingEmail(
    val recipientName: String,
    val propertyAddress: String,
    val jointLandlordEmails: List<String>,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val landlordInvitesKey = "landlord invites"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_NOTIFY_EXISTING_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            landlordInvitesKey to formatEmailList(jointLandlordEmails),
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
