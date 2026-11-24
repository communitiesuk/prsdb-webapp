package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class PropertyManagerAddedToPropertyEmail(
    val propertyManagerName: String,
    val propertyManagerEmail: String,
    val propertyAddress: String,
    val landlords: List<String>,
    val gdsStartPageUrl: String,
    val privacyNoticeUrl: String,
) : EmailTemplateModel {
    private val propertyManagerNameKey = "property manager name"
    private val propertyManagerEmailKey = "property manager email"
    private val propertyAddressKey = "property address"
    private val landlordsKey = "landlords"
    private val gdsStartPageUrlKey = "gds start page"
    private val privacyNoticeUrlKey = "privacy notice url"

    override val template = EmailTemplate.PROPERTY_MANAGER_ADDED_TO_PROPERTY

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            propertyManagerNameKey to propertyManagerName,
            propertyManagerEmailKey to propertyManagerEmail,
            propertyAddressKey to propertyAddress,
            landlordsKey to formatAsBulletList(landlords),
            gdsStartPageUrlKey to gdsStartPageUrl,
            privacyNoticeUrlKey to privacyNoticeUrl,
        )
}
