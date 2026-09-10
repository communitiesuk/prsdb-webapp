package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyUpdateWithLettingAgentRemovedConfirmation(
    val name: String,
    val propertyAddress: String,
    val updatedMessage: String,
    val lettingAgentEmail: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val nameKey = "name"
    private val propertyAddressKey = "property address"
    private val updatedMessageKey = "updated message"
    private val lettingAgentEmailKey = "letting agent email"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.PROPERTY_UPDATE_WITH_LETTING_AGENT_REMOVED_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            nameKey to name,
            propertyAddressKey to propertyAddress,
            updatedMessageKey to updatedMessage,
            lettingAgentEmailKey to lettingAgentEmail,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
