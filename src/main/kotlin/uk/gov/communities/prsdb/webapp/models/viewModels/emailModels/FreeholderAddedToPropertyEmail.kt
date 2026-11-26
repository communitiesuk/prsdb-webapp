package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class FreeholderAddedToPropertyEmail(
    val freeholderName: String,
    val freeholderEmail: String,
    val propertyAddress: String,
    val landlords: List<String>,
    val gdsStartPageUrl: String,
    val privacyNoticeUrl: String,
) : EmailTemplateModel {
    private val freeholderNameKey = "freeholder name"
    private val freeholderEmailKey = "freeholder email"
    private val propertyAddressKey = "property address"
    private val landlordsKey = "landlords"
    private val gdsStartPageKey = "gds start page"
    private val privacyNoticeKey = "privacy notice"

    override val template = EmailTemplate.FREEHOLDER_ADDED_TO_PROPERTY_EMAIL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            freeholderNameKey to freeholderName,
            freeholderEmailKey to freeholderEmail,
            propertyAddressKey to propertyAddress,
            landlordsKey to formatAsBulletList(landlords),
            gdsStartPageKey to gdsStartPageUrl,
            privacyNoticeKey to privacyNoticeUrl,
        )
}
