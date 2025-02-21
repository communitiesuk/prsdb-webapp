package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class ExampleEmail(
    var firstName: String,
) : EmailTemplateModel {
    private val firstNameKey = "first name"

    override val templateId = EmailTemplateId.EXAMPLE_EMAIL

    override fun toHashMap(): HashMap<String, String> = hashMapOf(firstNameKey to firstName)
}
