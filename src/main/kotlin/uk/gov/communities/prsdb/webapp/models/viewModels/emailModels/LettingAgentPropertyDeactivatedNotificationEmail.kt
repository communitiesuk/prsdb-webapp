package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LettingAgentPropertyDeactivatedNotificationEmail(
    val propertyAddress: String,
    val singleLineAddress: String,
) : EmailTemplateModel {
    private val propertyAddressKey = "property address"
    private val singleLineAddressKey = "single line address"

    override val template = EmailTemplate.LETTING_AGENT_PROPERTY_DEACTIVATED_NOTIFICATION_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            propertyAddressKey to propertyAddress,
            singleLineAddressKey to singleLineAddress,
        )
}
