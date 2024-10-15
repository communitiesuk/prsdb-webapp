package uk.gov.communities.prsdb.webapp.viewmodel

data class TestEmail(
    var firstName: String,
) : EmailTemplateModel {
    private val firstNameKey = "first name"

    override val templateId = "71551da6-f616-45c7-a4e0-23d6a8434561"

    override fun toHashMap(): HashMap<String, String> = hashMapOf(firstNameKey to firstName)
}
