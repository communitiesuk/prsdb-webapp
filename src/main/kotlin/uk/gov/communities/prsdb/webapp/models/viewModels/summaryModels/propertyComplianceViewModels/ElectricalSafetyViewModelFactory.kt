package uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.propertyComplianceViewModels

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.helpers.extensions.addRow
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.UploadService

@PrsdbWebService
class ElectricalSafetyViewModelFactory(
    private val uploadService: UploadService,
) {
    fun fromEntity(propertyCompliance: PropertyCompliance): List<SummaryListRowViewModel> =
        mutableListOf<SummaryListRowViewModel>()
            .apply {
                val visibleUploads =
                    propertyCompliance.electricalSafetyFileUploads.filter { it.status != FileUploadStatus.DELETED }
                val certKeyPrefix = getCertKeyPrefix(propertyCompliance.electricalCertType)
                val expired = propertyCompliance.isElectricalSafetyExpired
                addFileUploadRows(
                    visibleUploads = visibleUploads,
                    certificateKey = "$certKeyPrefix.certificate",
                    downloadMessageKey =
                        if (expired == true) "$certKeyPrefix.downloadExpiredCertificate" else "$certKeyPrefix.downloadCertificate",
                    noUploadMessageKey = getNonUploadStatusMessageKey(propertyCompliance),
                    fallbackFileName = "certificate",
                    uploadService = uploadService,
                )
                if (propertyCompliance.electricalSafetyExpiryDate != null) {
                    addRow(
                        key = "propertyDetails.complianceInformation.expiryDate",
                        value = propertyCompliance.electricalSafetyExpiryDate,
                    )
                } else {
                    addRow(
                        key = "propertyDetails.complianceInformation.exemption",
                        value = "propertyDetails.complianceInformation.noExemption",
                    )
                }
            }.toList()

    private fun getCertKeyPrefix(electricalCertType: CertificateType?): String =
        when (electricalCertType) {
            CertificateType.Eic -> "propertyDetails.complianceInformation.electricalSafety.eic"
            CertificateType.Eicr -> "propertyDetails.complianceInformation.electricalSafety.eicr"
            else -> DEFAULT_CERT_KEY_PREFIX
        }

    private fun getNonUploadStatusMessageKey(propertyCompliance: PropertyCompliance): String =
        when {
            propertyCompliance.isElectricalSafetyExpired == true -> "propertyDetails.complianceInformation.expired"
            else -> "propertyDetails.complianceInformation.notAdded"
        }

    companion object {
        private const val DEFAULT_CERT_KEY_PREFIX = "propertyDetails.complianceInformation.electricalSafety"
    }
}
