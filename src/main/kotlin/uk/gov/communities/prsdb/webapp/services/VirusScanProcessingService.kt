package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload
import uk.gov.communities.prsdb.webapp.database.repository.CertificateUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

@Service
class VirusScanProcessingService(
    private val dequarantiner: UploadDequarantiner,
    private val virusAlertSender: VirusAlertSender,
    private val certificateUploadRepository: CertificateUploadRepository,
    private val fileUploadRepository: FileUploadRepository,
) {
    @Transactional
    fun processScan(
        locator: UploadedFileLocator,
        scanResultStatus: ScanResult,
    ) {
        val certificateUpload = getCertificateUpload(locator)

        if (certificateUpload != null) {
            processCertificateScanResult(certificateUpload, scanResultStatus)
        } else {
            removeOrphanedFileUpload(locator)
        }
    }

    private fun processCertificateScanResult(
        certificateUpload: CertificateUpload,
        scanResultStatus: ScanResult,
    ) {
        when (scanResultStatus) {
            ScanResult.NoThreats -> {
                if (!dequarantiner.dequarantineFile(certificateUpload.fileUpload)) {
                    throw PrsdbWebException("Failed to dequarantine file: ${certificateUpload.fileUpload.objectKey}")
                }
            }
            ScanResult.Threats,
            ScanResult.Unsupported,
            ScanResult.Failed,
            -> {
                virusAlertSender.sendAlerts(certificateUpload.propertyOwnership, certificateUpload.category)
                if (!dequarantiner.deleteQuarantinedFile(certificateUpload.fileUpload)) {
                    throw PrsdbWebException("Failed to delete unsafe file: ${certificateUpload.fileUpload.objectKey}")
                }
            }
            ScanResult.AccessDenied -> throw PrsdbWebException(
                "GuardDuty does not have access to scan $certificateUpload.fileUpload.objectKey",
            )
        }
    }

    private fun removeOrphanedFileUpload(locator: UploadedFileLocator) {
        val fileUpload =
            fileUploadRepository.findByObjectKeyAndVersionId(locator.objectKey, locator.versionId)

        fileUpload?.let {
            if (dequarantiner.deleteQuarantinedFile(fileUpload)) {
                throw PrsdbWebException("Deleted orphaned file: ${fileUpload.objectKey}")
            } else {
                throw PrsdbWebException("Failed to delete orphaned file: ${fileUpload.objectKey}")
            }
        }
        throw PrsdbWebException(
            "No file upload found for object key: ${locator.objectKey} and version ID: ${locator.versionId}",
        )
    }

    private fun getCertificateUpload(certificateFileLocator: UploadedFileLocator): CertificateUpload? {
        val certificateUpload =
            certificateUploadRepository.findByFileUpload_ObjectKeyAndFileUpload_VersionId(
                objectKey = certificateFileLocator.objectKey,
                versionId = certificateFileLocator.versionId,
            ) ?: return null

        val fileETag = certificateUpload.fileUpload.eTag
        if (fileETag != certificateFileLocator.eTag) {
            throw PrsdbWebException(
                "ETag mismatch for object key: ${certificateFileLocator.objectKey}. " +
                    "Expected: $fileETag, Received: ${certificateFileLocator.eTag}",
            )
        }
        return certificateUpload
    }
}
