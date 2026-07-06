package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordYouLeftConfirmation(
    val recipientName: String,
    val propertyAddress: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"

    override val template = EmailTemplate.JOINT_LANDLORD_YOU_LEFT_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
        )
}
