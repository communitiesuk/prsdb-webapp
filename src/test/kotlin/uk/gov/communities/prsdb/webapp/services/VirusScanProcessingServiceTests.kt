package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

class VirusScanProcessingServiceTests {
    private lateinit var virusScanProcessingService: VirusScanProcessingService
    private lateinit var dequarantiner: FileDequarantiner

    private lateinit var virusAlertSender: VirusAlertSender

    @BeforeEach
    fun setup() {
        dequarantiner = mock()
        virusAlertSender = mock()
        virusScanProcessingService =
            VirusScanProcessingService(dequarantiner, virusAlertSender)
    }

    @Test
    fun `when no threats are found, processScan calls the dequarantiner`() {
        // Arrange
        val fileUpload =
            FileUpload(
                FileUploadStatus.QUARANTINED,
                "s3Key",
                "txt",
            )
        val fileNameInfo = CertificateUpload(fileUpload, FileCategory.GasSafetyCert, MockLandlordData.createPropertyOwnership())
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantineFile(any())).thenReturn(true)

        // Act
        virusScanProcessingService.processScan(fileNameInfo, scanResultStatus)

        // Assert
        verify(dequarantiner).dequarantineFile(fileUpload)
    }

    @Test
    fun `if the dequarantiner fails the processScan throws an exception`() {
        // Arrange
        val fileUpload =
            FileUpload(
                FileUploadStatus.QUARANTINED,
                "s3Key",
                "txt",
            )
        val fileNameInfo = CertificateUpload(fileUpload, FileCategory.GasSafetyCert, MockLandlordData.createPropertyOwnership())
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantineFile(any())).thenReturn(false)

        // Act & Assert
        assertThrows<PrsdbWebException> { virusScanProcessingService.processScan(fileNameInfo, scanResultStatus) }
    }

    @EnumSource(ScanResult::class)
    @ParameterizedTest
    fun `processScan throws an error for each scan result other than NoThreats`(scanResultStatus: ScanResult) {
        // Ignore NoThreats and AccessDenied cases since they are tested separately
        if (scanResultStatus == ScanResult.NoThreats || scanResultStatus == ScanResult.AccessDenied) {
            return
        }
        // Arrange
        val fileUpload =
            FileUpload(
                FileUploadStatus.QUARANTINED,
                "s3Key",
                "txt",
            )
        val fileNameInfo = CertificateUpload(fileUpload, FileCategory.GasSafetyCert, MockLandlordData.createPropertyOwnership())

        whenever(dequarantiner.deleteFile(any())).thenReturn(true)

        // Act
        virusScanProcessingService.processScan(fileNameInfo, scanResultStatus)

        // Assert
        verify(dequarantiner).deleteFile(fileUpload)
    }

    @Test
    fun `processScan throws an exception for AccessDenied scan result`() {
        // Arrange
        val fileUpload =
            FileUpload(
                FileUploadStatus.QUARANTINED,
                "s3Key",
                "txt",
            )
        val fileNameInfo = CertificateUpload(fileUpload, FileCategory.GasSafetyCert, MockLandlordData.createPropertyOwnership())

        val scanResultStatus = ScanResult.AccessDenied

        assertThrows<PrsdbWebException> { virusScanProcessingService.processScan(fileNameInfo, scanResultStatus) }
    }
}
