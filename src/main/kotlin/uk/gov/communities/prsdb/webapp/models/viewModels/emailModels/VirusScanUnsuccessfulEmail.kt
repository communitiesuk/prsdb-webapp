package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class VirusScanUnsuccessfulEmail(
    val certificateType: String,
    val recipientName: String,
    val propertyAddress: String,
    val registerRentalPropertyURL: URI,
) : EmailTemplateModel {
    private val certificateTypeKey = "certificate type"
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val registerRentalPropertyUrlKey = "register rental property url"

    override val template = EmailTemplate.VIRUS_SCAN_UNSUCCESSFUL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            certificateTypeKey to certificateType,
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            registerRentalPropertyUrlKey to registerRentalPropertyURL.toASCIIString(),
        )
}
