package uk.gov.communities.prsdb.webapp.models.viewModels.letterModels

import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.formatAsBulletList

data class SuperiorLandlordAddedToPropertyLetter(
    val superiorLandlordName: String,
    val propertyAddress: String,
    val landlords: List<String>,
    val superiorLandlordAddress: String,
    val privacyNoticeUrl: String,
) : LetterTemplateModel {
    override val template = LetterTemplate.SUPERIOR_LANDLORD_ADDED_TO_PROPERTY_LETTER

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            "superior landlord name" to superiorLandlordName,
            "property address" to propertyAddress,
            "landlords" to formatAsBulletList(landlords),
            "superior landlord address" to superiorLandlordAddress,
            "privacy notice" to privacyNoticeUrl,
        )
}
