package uk.gov.communities.prsdb.webapp.models.viewModels.emailModels

import java.net.URI

data class VirusScanUnsuccessfulEmail(
    val subjectCertificateType: String,
    val headingCertificateType: String,
    val bodyCertificateType: String,
    val singleLineAddress: String,
    val registrationNumber: String,
    val propertyUrl: URI,
) : EmailTemplateModel {
    private val subjectCertificateTypeKey = "subject certificate type"
    private val headingCertificateTypeKey = "heading certificate type"
    private val bodyCertificateTypeKey = "body certificate type"
    private val singleLineAddressKey = "single line address"
    private val registrationNumberKey = "registration number"
    private val propertyUrlKey = "property url"

    override val template = EmailTemplate.VIRUS_SCAN_UNSUCCESSFUL

    override fun toHashMap(): HashMap<String, String> =
        hashMapOf(
            subjectCertificateTypeKey to subjectCertificateType,
            headingCertificateTypeKey to headingCertificateType,
            bodyCertificateTypeKey to bodyCertificateType,
            singleLineAddressKey to singleLineAddress,
            registrationNumberKey to registrationNumber,
            propertyUrlKey to propertyUrl.toASCIIString(),
        )
}
