package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.FileNameInfo
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult

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
                if (!dequarantiner.dequarantine(fileNameInfo.toString())) {
                    throw PrsdbWebException("Failed to dequarantine file: $fileNameInfo")
                }
            }
            ScanResult.Threats -> TODO("PRSD-1284")
            ScanResult.Unsupported -> TODO("PRSD-1284")
            ScanResult.AccessDenied -> TODO("PRSD-1284")
            ScanResult.Failed -> TODO("PRSD-1284")
        }
    }
}
