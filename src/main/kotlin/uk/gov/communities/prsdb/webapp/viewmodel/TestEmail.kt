package uk.gov.communities.prsdb.webapp.viewmodel

data class TestEmail(
    var firstName: String,
) : EmailTemplateModel {
    private val firstNameKey = "first name"

    override val templateId = EmailTemplateId.TEST_EMAIL

    override fun toHashMap(): HashMap<String, String> = hashMapOf(firstNameKey to firstName)
}
