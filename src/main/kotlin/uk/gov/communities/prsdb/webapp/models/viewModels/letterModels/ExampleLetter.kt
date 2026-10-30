package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

// TODO PDJB-1988 / PDJB-1990: Remove EXAMPLE LETTER when the first real letter is added
data class ExampleLetter(
    val firstName: String,
) : LetterTemplateModel {
    override val template = LetterTemplate.EXAMPLE_LETTER

    override fun toHashMap(): HashMap<String, String> = hashMapOf("first name" to firstName)
}
