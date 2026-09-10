package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class DelegateToLettingAgentConfirmationEmail(
    val recipientName: String,
    val propertyAddress: String,
    val lettingAgentEmail: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val lettingAgentEmailKey = "letting agent email"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.DELEGATE_TO_LETTING_AGENT_CONFIRMATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            lettingAgentEmailKey to lettingAgentEmail,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
