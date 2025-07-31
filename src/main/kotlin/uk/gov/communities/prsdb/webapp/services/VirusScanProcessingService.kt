package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult

@Service
class VirusScanProcessingService(
    private val dequarantiner: FileDequarantiner,
    private val virusAlertSender: VirusAlertSender,
) {
    fun processScan(
        certificateUpload: CertificateUpload,
        scanResultStatus: ScanResult,
    ) {
        val ownership = certificateUpload.propertyOwnership

        when (scanResultStatus) {
            ScanResult.NoThreats -> {
                // TODO PRSD-976 - We might need to dequarantine to a more contextual object key if that is visible to users
                if (!dequarantiner.dequarantineFile(certificateUpload.fileUpload)) {
                    throw PrsdbWebException("Failed to dequarantine file: ${certificateUpload.fileUpload.objectKey}")
                }
            }
            ScanResult.Threats,
            ScanResult.Unsupported,
            ScanResult.Failed,
            -> {
                virusAlertSender.sendAlerts(ownership, certificateUpload.category)
                if (!dequarantiner.deleteFile(certificateUpload.fileUpload)) {
                    throw PrsdbWebException("Failed to delete unsafe file: ${certificateUpload.fileUpload.objectKey}")
                }
            }
            ScanResult.AccessDenied -> throw PrsdbWebException(
                "GuardDuty does not have access to scan $certificateUpload.fileUpload.objectKey",
            )
        }
    }
}
