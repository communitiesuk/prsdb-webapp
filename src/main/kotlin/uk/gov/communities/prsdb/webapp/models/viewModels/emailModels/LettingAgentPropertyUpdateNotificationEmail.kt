package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LettingAgentPropertyUpdateNotificationEmail(
    val recipientName: String,
    val propertyAddress: String,
    val registrationNumber: String,
    val updatedBullets: List<String>,
    val propertyRecordUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val registrationNumberKey = "registration number"
    private val updatedBulletsKey = "updated bullets"
    private val propertyRecordUrlKey = "property record url"

    override val template = EmailTemplate.LETTING_AGENT_PROPERTY_UPDATE_NOTIFICATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            registrationNumberKey to registrationNumber,
            updatedBulletsKey to formatAsBulletList(updatedBullets),
            propertyRecordUrlKey to propertyRecordUrl,
        )
}
