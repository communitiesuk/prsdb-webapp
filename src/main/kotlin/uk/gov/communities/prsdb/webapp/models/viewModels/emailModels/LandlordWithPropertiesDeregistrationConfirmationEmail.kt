package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class LandlordWithPropertiesDeregistrationConfirmationEmail(
    val propertyListMarkdown: PropertyDetailsEmailSectionList,
) : EmailTemplateModel {
    private val propertyListKey = "property list"

    override val templateId = EmailTemplateId.LANDLORD_WITH_PROPERTIES_DEREGISTRATION_CONFIRMATION

    override fun toHashMap(): HashMap<String, String> = hashMapOf(propertyListKey to propertyListMarkdown.toString())
}
