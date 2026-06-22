package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyDeregistrationConfirmationEmailRedesign(
    val landlordName: String,
    val multiLineAddress: String,
) : EmailTemplateModel {
    private val landlordNameKey = "landlord name"
    private val addressKey = "property address"

    override val template = EmailTemplate.PROPERTY_DEREGISTRATION_CONFIRMATION_REDESIGN

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            landlordNameKey to landlordName,
            addressKey to multiLineAddress,
        )
}
