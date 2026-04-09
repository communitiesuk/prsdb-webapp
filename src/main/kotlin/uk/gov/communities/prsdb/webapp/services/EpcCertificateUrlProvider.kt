package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.models.dataModels.EpcDataModel.Companion.parseCertificateNumberOrNull

@Service
class EpcCertificateUrlProvider(
    @Value("\${epc.certificate-base-url}")
    private val epcCertificateBaseUrl: String,
) {
    fun getEpcCertificateUrl(certificateNumber: String) = "$epcCertificateBaseUrl/${parseCertificateNumberOrNull(certificateNumber)}"
}
