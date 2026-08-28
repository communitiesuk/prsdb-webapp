package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class JointLandlordPropertyUpdateWithLettingAgentRemovedNotification(
    val recipientName: String,
    val propertyAddress: String,
    val updatedMessage: String,
    val lettingAgentEmail: String,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val updatedMessageKey = "updated message"
    private val lettingAgentEmailKey = "letting agent email"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.JOINT_LANDLORD_PROPERTY_UPDATE_WITH_LETTING_AGENT_REMOVED_NOTIFICATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            updatedMessageKey to updatedMessage,
            lettingAgentEmailKey to lettingAgentEmail,
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
