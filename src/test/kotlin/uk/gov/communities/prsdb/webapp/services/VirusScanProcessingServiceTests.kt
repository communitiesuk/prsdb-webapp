package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.repository.PropertyComplianceRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult

class VirusScanProcessingServiceTests {
    private lateinit var virusScanProcessingService: VirusScanProcessingService
    private lateinit var dequarantiner: FileDequarantiner

    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository
    private lateinit var complianceRepository: PropertyComplianceRepository

    @BeforeEach
    fun setup() {
        dequarantiner = mock()
        propertyOwnershipRepository = mock()
        complianceRepository = mock()
        virusScanProcessingService = VirusScanProcessingService(propertyOwnershipRepository, complianceRepository, dequarantiner)

        whenever(propertyOwnershipRepository.findByIdAndIsActiveTrue(anyLong())).thenAnswer { invocation ->
            val ownership = mock<PropertyOwnership>()
            whenever(ownership.id).thenReturn(invocation.getArgument(0))
            ownership
        }
    }

    @Test
    fun `when no threats are found, processScan calls the dequarantiner`() {
        // Arrange
        val fileNameInfo = PropertyFileNameInfo(5L, FileCategory.GasSafetyCert, "txt")
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantine(fileNameInfo.toString())).thenReturn(true)

        // Act
        virusScanProcessingService.processScan(fileNameInfo, scanResultStatus)

        // Assert
        verify(dequarantiner).dequarantine(fileNameInfo.toString())
    }

    @EnumSource(FileCategory::class)
    @ParameterizedTest
    fun `when the dequarantiner succeeds, processScan adds the certificate to the compliance record if present`(category: FileCategory) {
        // Arrange
        val fileNameInfo = PropertyFileNameInfo(5L, category, "jpg")
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantine(fileNameInfo.toString())).thenReturn(true)
        whenever(complianceRepository.findByPropertyOwnership_Id(fileNameInfo.propertyOwnershipId)).thenReturn(PropertyCompliance())

        // Act
        virusScanProcessingService.processScan(fileNameInfo, scanResultStatus)

        // Assert
        val captor = argumentCaptor<PropertyCompliance>()
        verify(complianceRepository).save(captor.capture())
        assert(getKeyFromRecordForCategory(captor.firstValue, category) == fileNameInfo.toString())
    }

    private fun getKeyFromRecordForCategory(
        complianceRecord: PropertyCompliance,
        category: FileCategory,
    ): String? =
        when (category) {
            FileCategory.Eirc -> complianceRecord.eicrS3Key
            FileCategory.GasSafetyCert -> complianceRecord.gasSafetyCertS3Key
        }

    @Test
    fun `if the dequarantiner fails the processScan throws an exception`() {
        // Arrange
        val fileNameInfo = PropertyFileNameInfo(5L, FileCategory.GasSafetyCert, "txt")
        val scanResultStatus = ScanResult.NoThreats

        whenever(dequarantiner.dequarantine(fileNameInfo.toString())).thenReturn(false)

        // Act & Assert
        assertThrows<PrsdbWebException> { virusScanProcessingService.processScan(fileNameInfo, scanResultStatus) }
    }

    @EnumSource(ScanResult::class)
    @ParameterizedTest
    fun `processScan throws an error for each scan result other than NoThreats`(scanResultStatus: ScanResult) {
        // Ignore NoThreats case since it is already tested
        if (scanResultStatus == ScanResult.NoThreats) {
            return
        }

        // Arrange
        val fileNameInfo = PropertyFileNameInfo(5L, FileCategory.GasSafetyCert, "txt")
        val scanResultStatus = scanResultStatus

        // TODO PRSD-1284
        assertThrows<NotImplementedError> { virusScanProcessingService.processScan(fileNameInfo, scanResultStatus) }
    }
}
