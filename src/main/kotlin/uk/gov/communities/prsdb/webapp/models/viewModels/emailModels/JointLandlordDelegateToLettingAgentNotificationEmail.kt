package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordDelegateToLettingAgentNotificationEmail(
    val recipientName: String,
    val propertyAddress: String,
    val lettingAgentEmail: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val lettingAgentEmailKey = "letting agent email"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_DELEGATE_TO_LETTING_AGENT_NOTIFICATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            lettingAgentEmailKey to lettingAgentEmail,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
