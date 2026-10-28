package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

data class ExampleLetter(
    val firstName: String,
) : LetterTemplateModel {
    override val template = LetterTemplate.EXAMPLE_LETTER

    override fun toHashMap(): HashMap<String, String> = hashMapOf("first name" to firstName)
}
