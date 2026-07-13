package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyDeregistrationConfirmationEmail(
    val landlordName: String,
    val multiLineAddress: String,
) : EmailTemplateModel {
    private val landlordNameKey = "landlord name"
    private val addressKey = "property address"

    override val template = EmailTemplate.PROPERTY_DEREGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            landlordNameKey to landlordName,
            addressKey to multiLineAddress,
        )
}
