package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

// TODO: https://mhclgdigital.atlassian.net/browse/PDJB-1701 - Remove recipientName and landlordDashboardUrl once Notify template V3 is live
data class VirusScanUnsuccessfulEmail(
    val certificateType: String,
    val recipientName: String,
    val propertyAddress: String,
    val landlordDashboardUrl: URI,
) : EmailTemplateModel {
    private val certificateTypeKey = "certificate type"
    private val recipientNameKey = "recipient name"
    private val propertyAddressKey = "property address"
    private val landlordDashboardUrlKey = "dashboard url"

    override val template = EmailTemplate.VIRUS_SCAN_UNSUCCESSFUL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            certificateTypeKey to certificateType,
            recipientNameKey to recipientName,
            propertyAddressKey to propertyAddress,
            landlordDashboardUrlKey to landlordDashboardUrl.toASCIIString(),
        )
}
