package uk.gov.communities.prsdb.webapp.services

import jakarta.transaction.Transactional
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback.Companion.extractFileUpload
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

@PrsdbTaskService
class VirusScanProcessingService(
    private val dequarantiner: UploadDequarantiner,
    private val virusCallbackHandler: VirusNotificationEmailHandler,
    private val virusScanCallbackRepository: VirusScanCallbackRepository,
    private val fileUploadRepository: FileUploadRepository,
) {
    @Transactional
    fun processScan(
        locator: UploadedFileLocator,
        scanResultStatus: ScanResult,
    ) {
        val callbackDetails = getCallbackDetails(locator)

        if (callbackDetails.isNotEmpty()) {
            processCertificateScanResult(callbackDetails, scanResultStatus)
        } else {
            removeOrphanedFileUpload(locator)
        }
    }

    private fun processCertificateScanResult(
        callbackDetails: List<VirusScanCallback>,
        scanResultStatus: ScanResult,
    ) {
        val fileUpload = callbackDetails.extractFileUpload()

        when (scanResultStatus) {
            ScanResult.NoThreats -> {
                if (!dequarantiner.dequarantineFile(fileUpload)) {
                    throw PrsdbWebException("Failed to dequarantine file: ${fileUpload.objectKey}")
                }
            }

            ScanResult.Threats,
            ScanResult.Unsupported,
            ScanResult.Failed,
            -> {
                callbackDetails.forEach { callback ->
                    virusCallbackHandler.handleCallback(callback)
                }
                if (!dequarantiner.deleteQuarantinedFile(fileUpload)) {
                    throw PrsdbWebException("Failed to delete unsafe file: ${fileUpload.objectKey}")
                }
            }

            ScanResult.AccessDenied -> {
                throw PrsdbWebException(
                    "GuardDuty does not have access to scan $callbackDetails.fileUpload.objectKey",
                )
            }
        }
        callbackDetails.forEach { virusScanCallbackRepository.delete(it) }
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

    private fun getCallbackDetails(certificateFileLocator: UploadedFileLocator): List<VirusScanCallback> {
        val callbacks =
            virusScanCallbackRepository.findAllByFileUpload_ObjectKeyAndFileUpload_VersionId(
                objectKey = certificateFileLocator.objectKey,
                versionId = certificateFileLocator.versionId,
            )
        callbacks.forEach { callback ->
            val fileETag = callback.fileUpload.eTag
            if (fileETag != certificateFileLocator.eTag) {
                throw PrsdbWebException(
                    "ETag mismatch for object key: ${certificateFileLocator.objectKey}. " +
                        "Expected: $fileETag, Received: ${certificateFileLocator.eTag}",
                )
            }
        }
        return callbacks
    }
}
