package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult

@Service
class VirusScanProcessingService(
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val complianceRepository: PropertyComplianceRepository,
    private val dequarantiner: FileDequarantiner,
    private val virusAlertSender: VirusAlertSender,
) {
    fun processScan(
        fileNameInfo: PropertyFileNameInfo,
        scanResultStatus: ScanResult,
    ) {
        val ownership = getOwnershipForFileInfo(fileNameInfo)
        when (scanResultStatus) {
            ScanResult.NoThreats -> {
                if (dequarantiner.dequarantineFile(fileNameInfo.toString())) {
                    addFileToComplianceRecordIfPresent(fileNameInfo, ownership)
                } else {
                    throw PrsdbWebException("Failed to dequarantine file: $fileNameInfo")
                }
            }
            ScanResult.Threats,
            ScanResult.Unsupported,
            ScanResult.Failed,
            -> {
                virusAlertSender.sendAlerts(ownership, fileNameInfo)
                if (!dequarantiner.deleteFile(fileNameInfo.toString())) {
                    throw PrsdbWebException("Failed to delete unsafe file: $fileNameInfo")
                }
            }
            ScanResult.AccessDenied -> throw PrsdbWebException("GuardDuty does not have access to scan $fileNameInfo")
        }
    }

    private fun getOwnershipForFileInfo(fileNameInfo: PropertyFileNameInfo) =
        propertyOwnershipRepository
            .findByIdAndIsActiveTrue(fileNameInfo.propertyOwnershipId)
            ?: throw PrsdbWebException("No ownership found for $fileNameInfo")

    private fun addFileToComplianceRecordIfPresent(
        fileNameInfo: PropertyFileNameInfo,
        ownership: PropertyOwnership,
    ) {
        val complianceRecord = complianceRepository.findByPropertyOwnership_Id(ownership.id)
        if (complianceRecord != null) {
            val fileUpload = FileUpload(FileUploadStatus.SCANNED, fileNameInfo.toString())
            when (fileNameInfo.fileCategory) {
                // TODO 1352 - Replace with file upload status updates over new uploads
                FileCategory.Eirc -> complianceRecord.eicrFileUpload = fileUpload
                FileCategory.GasSafetyCert -> complianceRecord.gasSafetyFileUpload = fileUpload
            }
            complianceRepository.save(complianceRecord)
        }
    }
}
