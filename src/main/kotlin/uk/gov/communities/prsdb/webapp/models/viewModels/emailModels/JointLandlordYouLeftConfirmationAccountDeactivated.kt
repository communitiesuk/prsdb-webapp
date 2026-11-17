package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordYouLeftConfirmationAccountDeactivated(
    val recipientName: String,
    val propertyAddress: String,
    val dashboardUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val dashboardUrlKey = "dashboard url"

    override val template = EmailTemplate.JOINT_LANDLORD_YOU_LEFT_CONFIRMATION_ACCOUNT_DEACTIVATED

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            dashboardUrlKey to dashboardUrl,
        )
}
