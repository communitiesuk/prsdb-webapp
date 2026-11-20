package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

interface LetterTemplateModel {
    val template: LetterTemplate

    fun toHashMap(): HashMap<String, String>
}

enum class LetterTemplate {
    FREEHOLDER_ADDED_TO_PROPERTY_LETTER,
    SUPERIOR_LANDLORD_ADDED_TO_PROPERTY_LETTER,
}
