package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordInvitationCancellationInviteeEmail(
    val propertyAddress: String,
) : EmailTemplateModel {
    private val propertyAddressKey = "property address"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_CANCELLATION_INVITEE_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            propertyAddressKey to propertyAddress,
        )
}
