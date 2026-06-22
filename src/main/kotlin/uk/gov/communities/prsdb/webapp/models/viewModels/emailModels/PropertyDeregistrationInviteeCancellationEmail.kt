package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyDeregistrationInviteeCancellationEmail(
    val multiLineAddress: String,
    val signInUrl: String,
) : EmailTemplateModel {
    private val addressKey = "property address"
    private val signInUrlKey = "sign in url"

    override val template = EmailTemplate.PROPERTY_DEREGISTRATION_INVITEE_CANCELLATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            addressKey to multiLineAddress,
            signInUrlKey to signInUrl,
        )
}
