package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

// TODO PDJB-2270: Add the deactivation reason to the email copy once PDJB-2027 adds the property deactivation reason enum
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
