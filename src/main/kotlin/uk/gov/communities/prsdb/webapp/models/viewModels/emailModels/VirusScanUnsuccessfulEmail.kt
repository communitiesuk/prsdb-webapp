package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

data class VirusScanUnsuccessfulEmail(
    val certificateType: String,
    val propertyAddress: String,
) : EmailTemplateModel {
    private val certificateTypeKey = "certificate type"
    private val propertyAddressKey = "property address"

    override val template = EmailTemplate.VIRUS_SCAN_UNSUCCESSFUL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            certificateTypeKey to certificateType,
            propertyAddressKey to propertyAddress,
        )
}
