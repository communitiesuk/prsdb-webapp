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
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
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
    private lateinit var featureFlagManager: FeatureFlagManager

    private val fileUpload = FileUpload(FileUploadStatus.QUARANTINED, "eicr-1", "pdf", "etag1", "v1")

    @BeforeEach
    fun setup() {
        virusScanCallbackRepository = mock()
        fileUploadRepository = mock()
        featureFlagManager = mock()
        virusScanCallbackService =
            VirusScanCallbackService(virusScanCallbackRepository, fileUploadRepository, featureFlagManager)
    }

    private fun callbackFor(data: EmailNotificationData) = VirusScanCallback(fileUpload, Json.encodeToString<EmailNotificationData>(data))

    @Test
    fun `saveEmailForJourney saves a journey notification callback`() {
        // Arrange
        whenever(fileUploadRepository.getReferenceById(42L)).thenReturn(fileUpload)
        whenever(virusScanCallbackRepository.save(any())).thenAnswer { it.arguments[0] }

        // Act
        virusScanCallbackService.saveEmailForJourney("journey-1", 42L, CertificateType.Eicr, 7L)

        // Assert
        val captor = argumentCaptor<VirusScanCallback>()
        verify(virusScanCallbackRepository).save(captor.capture())
        assertEquals(
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, 7L),
            Json.decodeFromString<EmailNotificationData>(captor.firstValue.encodedCallbackData),
        )
    }

    @Test
    fun `saveEmailToMonitoringTeam saves a monitoring callback containing a journey notification`() {
        // Arrange
        whenever(fileUploadRepository.getReferenceById(42L)).thenReturn(fileUpload)
        whenever(virusScanCallbackRepository.save(any())).thenAnswer { it.arguments[0] }

        // Act
        virusScanCallbackService.saveEmailToMonitoringTeam("journey-1", 42L, CertificateType.Eicr, 7L)

        // Assert
        val captor = argumentCaptor<VirusScanCallback>()
        verify(virusScanCallbackRepository).save(captor.capture())
        assertEquals(
            EmailNotificationData.VirusMonitoringEmailNotification(
                EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, 7L),
            ),
            Json.decodeFromString<EmailNotificationData>(captor.firstValue.encodedCallbackData),
        )
    }

    @Test
    fun `saveEmailForUpdateJourney saves an owner notification callback`() {
        // Arrange
        whenever(fileUploadRepository.getReferenceById(42L)).thenReturn(fileUpload)
        whenever(virusScanCallbackRepository.save(any())).thenAnswer { it.arguments[0] }

        // Act
        virusScanCallbackService.saveEmailForUpdateJourney(99L, 42L, CertificateType.Eicr)

        // Assert
        val captor = argumentCaptor<VirusScanCallback>()
        verify(virusScanCallbackRepository).save(captor.capture())
        assertEquals(
            EmailNotificationData.OwnerEmailNotification(99L, CertificateType.Eicr),
            Json.decodeFromString<EmailNotificationData>(captor.firstValue.encodedCallbackData),
        )
    }

    @Test
    fun `saveEmailToMonitoringTeamForUpdateJourney saves a monitoring callback containing an owner notification`() {
        // Arrange
        whenever(fileUploadRepository.getReferenceById(42L)).thenReturn(fileUpload)
        whenever(virusScanCallbackRepository.save(any())).thenAnswer { it.arguments[0] }

        // Act
        virusScanCallbackService.saveEmailToMonitoringTeamForUpdateJourney(99L, 42L, CertificateType.Eicr)

        // Assert
        val captor = argumentCaptor<VirusScanCallback>()
        verify(virusScanCallbackRepository).save(captor.capture())
        assertEquals(
            EmailNotificationData.VirusMonitoringEmailNotification(
                EmailNotificationData.OwnerEmailNotification(99L, CertificateType.Eicr),
            ),
            Json.decodeFromString<EmailNotificationData>(captor.firstValue.encodedCallbackData),
        )
    }

    @Test
    fun `saveVirusScanFailureEmail saves owner-targeted callbacks when a property ownership id is present`() {
        // Arrange
        whenever(fileUploadRepository.getReferenceById(42L)).thenReturn(fileUpload)
        whenever(virusScanCallbackRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)

        // Act
        virusScanCallbackService.saveVirusScanFailureEmail(
            journeyId = "journey-1",
            fileUploadId = 42L,
            certificateType = CertificateType.Eicr,
            propertyOwnershipId = 99L,
            landlordId = 7L,
        )

        // Assert
        val captor = argumentCaptor<VirusScanCallback>()
        verify(virusScanCallbackRepository, times(2)).save(captor.capture())
        val savedData = captor.allValues.map { Json.decodeFromString<EmailNotificationData>(it.encodedCallbackData) }
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
    fun `saveVirusScanFailureEmail saves journey-targeted callbacks for an update when letting agent delegation is disabled`() {
        // Arrange
        whenever(fileUploadRepository.getReferenceById(42L)).thenReturn(fileUpload)
        whenever(virusScanCallbackRepository.save(any())).thenAnswer { it.arguments[0] }
        whenever(featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)

        // Act
        virusScanCallbackService.saveVirusScanFailureEmail(
            journeyId = "journey-1",
            fileUploadId = 42L,
            certificateType = CertificateType.Eicr,
            propertyOwnershipId = 99L,
            landlordId = 7L,
        )

        // Assert
        val captor = argumentCaptor<VirusScanCallback>()
        verify(virusScanCallbackRepository, times(2)).save(captor.capture())
        val savedData = captor.allValues.map { Json.decodeFromString<EmailNotificationData>(it.encodedCallbackData) }
        assertEquals(
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, 7L),
            savedData.single { it is EmailNotificationData.IncompletePropertyEmailNotification },
        )
        assertEquals(
            EmailNotificationData.VirusMonitoringEmailNotification(
                EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, 7L),
            ),
            savedData.single { it is EmailNotificationData.VirusMonitoringEmailNotification },
        )
    }

    @Test
    fun `saveVirusScanFailureEmail saves journey-targeted callbacks when there is no property ownership id but there is a landlord`() {
        // Arrange
        whenever(fileUploadRepository.getReferenceById(42L)).thenReturn(fileUpload)
        whenever(virusScanCallbackRepository.save(any())).thenAnswer { it.arguments[0] }

        // Act
        virusScanCallbackService.saveVirusScanFailureEmail(
            journeyId = "journey-1",
            fileUploadId = 42L,
            certificateType = CertificateType.Eicr,
            propertyOwnershipId = null,
            landlordId = 7L,
        )

        // Assert
        val captor = argumentCaptor<VirusScanCallback>()
        verify(virusScanCallbackRepository, times(2)).save(captor.capture())
        val savedData = captor.allValues.map { Json.decodeFromString<EmailNotificationData>(it.encodedCallbackData) }
        assertEquals(
            EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, 7L),
            savedData.single { it is EmailNotificationData.IncompletePropertyEmailNotification },
        )
        assertEquals(
            EmailNotificationData.VirusMonitoringEmailNotification(
                EmailNotificationData.IncompletePropertyEmailNotification("journey-1", CertificateType.Eicr, 7L),
            ),
            savedData.single { it is EmailNotificationData.VirusMonitoringEmailNotification },
        )
    }

    @Test
    fun `saveVirusScanFailureEmail does nothing when there is no property ownership id and no landlord`() {
        // Act
        virusScanCallbackService.saveVirusScanFailureEmail(
            journeyId = "journey-1",
            fileUploadId = 42L,
            certificateType = CertificateType.Eicr,
            propertyOwnershipId = null,
            landlordId = null,
        )

        // Assert
        verify(virusScanCallbackRepository, never()).save(any())
    }

    @Test
    fun `updateCallbacksToOwner re-points journey-target callbacks to the owner in place`() {
        // Arrange
        val directCallback =
            callbackFor(
                EmailNotificationData.IncompletePropertyEmailNotification(
                    "journey-1",
                    CertificateType.Eicr,
                    7L,
                ),
            )
        val monitoringCallback =
            callbackFor(
                EmailNotificationData.VirusMonitoringEmailNotification(
                    EmailNotificationData.IncompletePropertyEmailNotification(
                        "journey-1",
                        CertificateType.Eicr,
                        7L,
                    ),
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
