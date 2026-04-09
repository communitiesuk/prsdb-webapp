package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository

@PrsdbWebService
class VirusScanCallbackService(
    private val virusScanCallbackRepository: VirusScanCallbackRepository,
    private val fileUploadRepository: FileUploadRepository,
) {
    fun saveEmailToOwner(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val data =
            EmailNotificationData.OwnerEmailNotification(
                propertyOwnershipId = propertyOwnershipId,
                certificateType = certificateType,
            )

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    fun saveEmailForJourney(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val data =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
            )

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    fun saveEmailToMonitoringTeam(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val internalData =
            EmailNotificationData.OwnerEmailNotification(
                propertyOwnershipId = propertyOwnershipId,
                certificateType = certificateType,
            )

        val data = EmailNotificationData.VirusMonitoringEmailNotification(internalData)

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    fun saveEmailToMonitoringTeam(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val internalData =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
            )
        val data = EmailNotificationData.VirusMonitoringEmailNotification(internalData)

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    fun deleteAllCallbacksForFileUpload(fileUploadId: Long) {
        virusScanCallbackRepository.deleteAllByFileUpload_Id(fileUploadId)
    }
}

// This sealed class represents the different types of email notifications that can be triggered by a virus scan callback.
// If, in the future, we need to add callbacks that are not email notifications, we should create a new sealed class containing
// both this sealed class and the new types of callbacks, rather than adding non-email callback types to this class.
// There will also be a simple refactor to create a VirusCallbackHandler that wraps the VirusNotificationEmailHandler,
// which will allow us to handle non-email callbacks without overcomplicating the email handler.
@Serializable
sealed class EmailNotificationData {
    @Serializable
    data class OwnerEmailNotification(
        val propertyOwnershipId: Long,
        val certificateType: CertificateType,
    ) : EmailNotificationData()

    @Serializable
    data class IncompletePropertyEmailNotification(
        val journeyId: String,
        val certificateType: CertificateType,
    ) : EmailNotificationData()

    @Serializable
    data class VirusMonitoringEmailNotification(
        val internalEmailData: EmailNotificationData,
    ) : EmailNotificationData()
}
