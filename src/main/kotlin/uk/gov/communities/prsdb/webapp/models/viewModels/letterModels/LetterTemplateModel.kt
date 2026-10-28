package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

interface LetterTemplateModel {
    val template: LetterTemplate

    fun toHashMap(): HashMap<String, String>
}

enum class LetterTemplate {
    // An example letter for checking that letters can be sent; real letters need their own template and model
    EXAMPLE_LETTER,
}
