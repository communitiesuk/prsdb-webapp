package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyDeactivationConfirmationEmail(
    val recipientName: String,
    val multiLineAddress: String,
    val registrationNumber: String,
    val landlordDeactivated: Boolean,
    val dashboardUrl: String,
) : EmailTemplateModel {
    private val recipientNameKey = "recipient name"
    private val addressKey = "property address"
    private val registrationNumberKey = "prn"
    private val dashboardUrlKey = "dashboard url"

    override val template =
        if (landlordDeactivated) {
            EmailTemplate.PROPERTY_DEACTIVATION_CONFIRMATION_ACCOUNT_DEACTIVATED
        } else {
            EmailTemplate.PROPERTY_DEACTIVATION_CONFIRMATION
        }

    override fun toHashMap(): HashMap<String, String> {
        val hashMap =
            hashMapOf(
                recipientNameKey to recipientName,
                addressKey to multiLineAddress,
                registrationNumberKey to registrationNumber,
            )
        if (landlordDeactivated) {
            hashMap[dashboardUrlKey] = dashboardUrl
        }
        return hashMap
    }
}
