package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordInvitationAcceptedEmail(
    val recipientName: String,
    val propertyAddress: String,
    val propertyRecordUrl: String,
    val propertyRegistrationNumber: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val propertyRecordUrlKey = "property record url"
    private val propertyRegistrationNumberKey = "property registration number"

    override val template = EmailTemplate.JOINT_LANDLORD_INVITATION_ACCEPTED_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            propertyRecordUrlKey to propertyRecordUrl,
            propertyRegistrationNumberKey to propertyRegistrationNumber,
        )
}
