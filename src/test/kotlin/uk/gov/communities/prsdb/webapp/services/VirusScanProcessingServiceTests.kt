package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.FileNameInfo
import uk.gov.communities.prsdb.webapp.models.dataModels.FileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult

class VirusScanProcessingServiceTests {
    private lateinit var virusScanProcessingService: VirusScanProcessingService
    private lateinit var dequarantiner: FileDequarantiner

    @BeforeEach
    fun setup() {
        dequarantiner = mock()
        virusScanProcessingService = VirusScanProcessingService(dequarantiner)
    }

    @Test
    fun `processScan calls the dequarantiner when no threats are found`() {
        val fileNameInfo = FileNameInfo(5L, FileCategory.GasSafetyCert, "txt")
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantine(fileNameInfo.toString())).thenReturn(true)

        virusScanProcessingService.processScan(fileNameInfo, scanResultStatus)

        verify(dequarantiner).dequarantine(fileNameInfo.toString())
    }

    @Test
    fun `if the dequarantiner fails the processScan throws an exception`() {
        val fileNameInfo = FileNameInfo(5L, FileCategory.GasSafetyCert, "txt")
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantine(fileNameInfo.toString())).thenReturn(false)

        assertThrows<PrsdbWebException> { virusScanProcessingService.processScan(fileNameInfo, scanResultStatus) }
    }

    @EnumSource(ScanResult::class)
    @ParameterizedTest
    fun `processScan throws an error for each scan result other than NoThreats`(scanResultStatus: ScanResult) {
        if (scanResultStatus == ScanResult.NoThreats) {
            return
        }

        val fileNameInfo = FileNameInfo(5L, FileCategory.GasSafetyCert, "txt")
        val scanResultStatus = scanResultStatus

        // TODO PRSD-1284
        assertThrows<NotImplementedError> { virusScanProcessingService.processScan(fileNameInfo, scanResultStatus) }
    }
}
