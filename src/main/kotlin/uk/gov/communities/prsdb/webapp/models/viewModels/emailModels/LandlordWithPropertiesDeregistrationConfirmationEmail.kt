package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LandlordWithPropertiesDeregistrationConfirmationEmail(
    val fullName: String,
    // TODO PDJB-1522: Remove legacy property-list placeholder support after v1 template retirement.
    val propertyListMarkdown: PropertyDetailsEmailSectionList,
) : EmailTemplateModel {
    private val fullNameKey = "fullName"
    private val propertyListKey = "property list"

    override val template = EmailTemplate.LANDLORD_WITH_PROPERTIES_DEREGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            fullNameKey to fullName,
            propertyListKey to propertyListMarkdown.toString(),
        )
}
