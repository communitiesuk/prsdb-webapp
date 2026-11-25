package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyDeactivationInviteeCancellationEmail(
    val multiLineAddress: String,
) : EmailTemplateModel {
    private val addressKey = "property address"

    override val template = EmailTemplate.PROPERTY_DEACTIVATION_INVITEE_CANCELLATION

    override fun toHashMap(): HashMap<String, String> = hashMapOf(addressKey to multiLineAddress)
}
