package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.PrsdbService
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel.Companion.parseCertificateNumberOrNull

@PrsdbService
class EpcCertificateUrlProvider(
    @Value("\${epc.certificate-base-url}")
    private val epcCertificateBaseUrl: String,
) {
    fun getEpcCertificateUrl(certificateNumber: String) = "$epcCertificateBaseUrl/${parseCertificateNumberOrNull(certificateNumber)}"
}
