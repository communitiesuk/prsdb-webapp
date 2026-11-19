package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.formatAsBulletList

data class FreeholderAddedToPropertyLetter(
    val freeholderName: String,
    val propertyAddress: String,
    val landlords: List<String>,
    val freeholderAddress: String,
    val privacyNoticeUrl: String,
) : LetterTemplateModel {
    override val template = LetterTemplate.FREEHOLDER_ADDED_TO_PROPERTY_LETTER

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            "freeholder name" to freeholderName,
            "property address" to propertyAddress,
            "landlords" to formatAsBulletList(landlords),
            "freeholder address" to freeholderAddress,
            "privacy notice" to privacyNoticeUrl,
        )
}
