package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.CertificateUpload
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.repository.CertificateUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator

@ExtendWith(MockitoExtension::class)
class VirusScanProcessingServiceTests {
    @Mock
    private lateinit var dequarantiner: FileDequarantiner

    @Mock
    private lateinit var certificateUploadRepository: CertificateUploadRepository

    @Mock
    private lateinit var fileUploadRepository: FileUploadRepository

    @Mock
    private lateinit var virusAlertSender: VirusAlertSender

    @InjectMocks
    private lateinit var virusScanProcessingService: VirusScanProcessingService

    @Test
    fun `when no threats are found, processScan calls the dequarantiner`() {
        // Arrange
        val fileUpload =
            FileUpload(
                FileUploadStatus.QUARANTINED,
                "s3Key",
                "txt",
                "eTag",
                "versionId",
            )
        val locator = UploadedFileLocator(fileUpload.objectKey, fileUpload.eTag, fileUpload.versionId)
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantineFile(any())).thenReturn(true)
        whenever(certificateUploadRepository.findByFileUpload_ObjectKeyAndFileUpload_VersionId(any(), any()))
            .thenReturn(CertificateUpload(fileUpload, mock(), mock()))

        // Act
        virusScanProcessingService.processScan(locator, scanResultStatus)

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
                "eTag",
                "versionId",
            )
        val locator = UploadedFileLocator(fileUpload.objectKey, fileUpload.eTag, fileUpload.versionId)
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantineFile(any())).thenReturn(false)
        whenever(certificateUploadRepository.findByFileUpload_ObjectKeyAndFileUpload_VersionId(any(), any()))
            .thenReturn(CertificateUpload(fileUpload, mock(), mock()))

        // Act & Assert
        assertThrows<PrsdbWebException> { virusScanProcessingService.processScan(locator, scanResultStatus) }
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
                "eTag",
                "versionId",
            )
        val locator = UploadedFileLocator(fileUpload.objectKey, fileUpload.eTag, fileUpload.versionId)
        whenever(certificateUploadRepository.findByFileUpload_ObjectKeyAndFileUpload_VersionId(any(), any()))
            .thenReturn(CertificateUpload(fileUpload, mock(), mock()))

        whenever(dequarantiner.deleteFile(any())).thenReturn(true)

        // Act
        virusScanProcessingService.processScan(locator, scanResultStatus)

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
                "eTag",
                "versionId",
            )
        val locator = UploadedFileLocator(fileUpload.objectKey, fileUpload.eTag, fileUpload.versionId)

        val scanResultStatus = ScanResult.AccessDenied

        assertThrows<PrsdbWebException> { virusScanProcessingService.processScan(locator, scanResultStatus) }
    }
}
