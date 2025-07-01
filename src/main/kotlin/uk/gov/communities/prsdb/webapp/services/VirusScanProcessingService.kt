package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.services.UploadedFilenameParser.Companion.FileNameInfo

@Service
class VirusScanProcessingService(
    private val dequarantiner: FileDequarantiner,
) {
    fun processScan(
        fileNameInfo: FileNameInfo,
        scanResultStatus: ScanResult,
    ) {
        when (scanResultStatus) {
            ScanResult.NoThreats -> {
                dequarantiner.dequarantine(fileNameInfo.toObjectKey())
            }
            ScanResult.Threats -> TODO("PRSD-1284")
            ScanResult.Unsupported -> TODO("PRSD-1284")
            ScanResult.AccessDenied -> TODO("PRSD-1284")
            ScanResult.Failed -> TODO("PRSD-1284")
        }
    }
}
