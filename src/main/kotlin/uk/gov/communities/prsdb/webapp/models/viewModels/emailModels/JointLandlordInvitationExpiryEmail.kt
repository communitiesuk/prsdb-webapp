package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class JointLandlordInvitationExpiryEmail(
    val recipientName: String,
    val invitedEmail: String,
    val propertyAddress: String,
    val propertyRecordUri: URI,
    val expiryDays: Int,
) : EmailTemplateModel {
    private val recipientNameKey = "recipientName"
    private val invitedEmailKey = "invitedEmail"
    private val propertyAddressKey = "propertyAddress"
    private val propertyRecordUrlKey = "propertyRecordUrl"
    private val expiryDaysKey = "expiryDays"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_EXPIRY_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            invitedEmailKey to invitedEmail,
            propertyAddressKey to propertyAddress,
            propertyRecordUrlKey to propertyRecordUri.toString(),
            expiryDaysKey to expiryDays.toString(),
        )
}
