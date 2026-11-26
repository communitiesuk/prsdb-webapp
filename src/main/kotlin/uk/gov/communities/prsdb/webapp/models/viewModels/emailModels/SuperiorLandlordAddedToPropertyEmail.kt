package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class SuperiorLandlordAddedToPropertyEmail(
    val superiorLandlordName: String,
    val superiorLandlordEmail: String,
    val propertyAddress: String,
    val landlords: List<String>,
    val gdsStartPageUrl: String,
    val privacyNoticeUrl: String,
) : EmailTemplateModel {
    private val superiorLandlordNameKey = "superior landlord name"
    private val superiorLandlordEmailKey = "superior landlord email"
    private val propertyAddressKey = "property address"
    private val landlordsKey = "landlords"
    private val gdsStartPageKey = "gds start page"
    private val privacyNoticeKey = "privacy notice"

    override val template = EmailTemplate.SUPERIOR_LANDLORD_ADDED_TO_PROPERTY_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            superiorLandlordNameKey to superiorLandlordName,
            superiorLandlordEmailKey to superiorLandlordEmail,
            propertyAddressKey to propertyAddress,
            landlordsKey to formatAsBulletList(landlords),
            gdsStartPageKey to gdsStartPageUrl,
            privacyNoticeKey to privacyNoticeUrl,
        )
}
