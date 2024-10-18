package uk.gov.communities.prsdb.webapp.models.viewModels

interface EmailTemplateModel {
    val templateId: EmailTemplateId

    fun toHashMap(): HashMap<String, String>
}

enum class EmailTemplateId(
    val idValue: String,
) {
    EXAMPLE_EMAIL("71551da6-f616-45c7-a4e0-23d6a8434561"),
    LOCAL_AUTHORITY_INVITATION_EMAIL("66924ee6-40a3-4330-b502-0dbf288f71e5"),
}
