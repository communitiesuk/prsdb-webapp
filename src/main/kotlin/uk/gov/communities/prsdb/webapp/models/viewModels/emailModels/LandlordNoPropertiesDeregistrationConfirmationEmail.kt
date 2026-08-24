package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LandlordNoPropertiesDeregistrationConfirmationEmail(
    val fullName: String,
) : EmailTemplateModel {
    private val fullNameKey = "fullName"

    override val template = EmailTemplate.LANDLORD_NO_PROPERTIES_DEREGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> = hashMapOf(fullNameKey to fullName)
}
