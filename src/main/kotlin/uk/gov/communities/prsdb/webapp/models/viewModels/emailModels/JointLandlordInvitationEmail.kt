package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class JointLandlordInvitationEmail(
    val senderName: String,
    val propertyAddress: String,
    val invitationUri: URI,
) : EmailTemplateModel {
    private val senderNameKey = "senderName"
    private val propertyAddressesKey = "propertyAddresses"
    private val invitationUrlKey = "invitation url"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            senderNameKey to senderName,
            propertyAddressesKey to propertyAddress,
            invitationUrlKey to invitationUri.toString(),
        )
}
