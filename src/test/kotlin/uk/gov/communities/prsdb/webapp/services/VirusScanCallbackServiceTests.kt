package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.constants.enums.FileUploadStatus
import uk.gov.communities.prsdb.webapp.database.entity.FileUpload
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository
import kotlin.test.assertEquals

class VirusScanCallbackServiceTests {
    private lateinit var virusScanCallbackService: VirusScanCallbackService
    private lateinit var virusScanCallbackRepository: VirusScanCallbackRepository
    private lateinit var fileUploadRepository: FileUploadRepository

    private val fileUpload = FileUpload(FileUploadStatus.QUARANTINED, "eicr-1", "pdf", "etag1", "v1")

    @BeforeEach
    fun setup() {
        virusScanCallbackRepository = mock()
        fileUploadRepository = mock()
        virusScanCallbackService = VirusScanCallbackService(virusScanCallbackRepository, fileUploadRepository)
    }

    private fun callbackFor(data: EmailNotificationData) = VirusScanCallback(fileUpload, Json.encodeToString<EmailNotificationData>(data))

    @Test
    fun `updateCallbacksToOwner re-points journey-target callbacks to the owner in place`() {
        // Arrange
        val directCallback =
            callbackFor(EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr))
        val monitoringCallback =
            callbackFor(
                EmailNotificationData.VirusMonitoringEmailNotification(
                    EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr),
                ),
            )
        whenever(virusScanCallbackRepository.findAllByFileUpload_Id(42L))
            .thenReturn(listOf(directCallback, monitoringCallback))

        // Act
        virusScanCallbackService.updateCallbacksToOwner(42L, 99L, CertificateType.Eicr)

        // Assert
        val captor = argumentCaptor<String>()
        verify(virusScanCallbackRepository, times(2)).updateEncodedCallbackDataById(any(), captor.capture())
        val savedData = captor.allValues.map { Json.decodeFromString<EmailNotificationData>(it) }

        assertEquals(
            EmailNotificationData.OwnerEmailNotification(99L, CertificateType.Eicr),
            savedData.single { it is EmailNotificationData.OwnerEmailNotification },
        )
        assertEquals(
            EmailNotificationData.VirusMonitoringEmailNotification(
                EmailNotificationData.OwnerEmailNotification(99L, CertificateType.Eicr),
            ),
            savedData.single { it is EmailNotificationData.VirusMonitoringEmailNotification },
        )
    }

    @Test
    fun `updateCallbacksToOwner does nothing when the file upload has no callbacks`() {
        // Arrange
        whenever(virusScanCallbackRepository.findAllByFileUpload_Id(42L)).thenReturn(emptyList())

        // Act
        virusScanCallbackService.updateCallbacksToOwner(42L, 99L, CertificateType.Eicr)

        // Assert
        verify(virusScanCallbackRepository, never()).updateEncodedCallbackDataById(any(), any())
    }
}
