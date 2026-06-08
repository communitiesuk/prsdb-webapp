package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordInvitationConfirmationEmail(
    val senderName: String,
    val propertyAddress: String,
    val jointLandlordEmails: List<String>,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val senderNameKey = "sender name"
    private val propertyAddressKey = "property address"
    private val landlordInvitesKey = "landlord invites"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_CONFIRMATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            senderNameKey to senderName,
            propertyAddressKey to propertyAddress,
            landlordInvitesKey to formatEmailList(jointLandlordEmails),
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
