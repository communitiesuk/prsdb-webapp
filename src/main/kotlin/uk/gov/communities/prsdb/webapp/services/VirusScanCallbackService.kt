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
    fun saveEmailForJourney(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
        landlordId: Long,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val data =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
                landlordId = landlordId,
            )

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
        landlordId: Long,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val internalData =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
                landlordId = landlordId,
            )
        val data = EmailNotificationData.VirusMonitoringEmailNotification(internalData)

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    // Used when the upload happens on an update journey for a property that is already registered, so the
    // owning property (and therefore all its landlords and its letting agent) is already known. Saving an
    // OwnerEmailNotification directly - rather than an IncompletePropertyEmailNotification keyed on the
    // uploading user - means the eventual failure alert always reaches every landlord and the letting agent,
    // regardless of which of them uploaded the certificate.
    fun saveEmailForOwnership(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val data = EmailNotificationData.OwnerEmailNotification(propertyOwnershipId, certificateType)

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    fun saveEmailToMonitoringTeamForOwnership(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        val internalData = EmailNotificationData.OwnerEmailNotification(propertyOwnershipId, certificateType)
        val data = EmailNotificationData.VirusMonitoringEmailNotification(internalData)

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    // Single entry point shared by every certificate-upload step (gas safety, electrical safety, etc.) so the
    // "which notification shape do we need?" decision is made in one place rather than duplicated per step config.
    //
    // - Update journeys (propertyOwnershipId non-null): the property is already registered, so we know every
    //   landlord and the letting agent regardless of who uploaded - save an ownership-targeted notification.
    // - Registration journeys (propertyOwnershipId null): no property ownership exists yet, so the notification
    //   is tied to the uploading landlord's in-progress journey until the property is registered. If there is no
    //   acting landlord (registration is always landlord-led), nothing is saved.
    fun saveVirusScanFailureEmail(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
        propertyOwnershipId: Long?,
        landlordId: Long?,
    ) {
        if (propertyOwnershipId != null) {
            saveEmailForOwnership(propertyOwnershipId, fileUploadId, certificateType)
            saveEmailToMonitoringTeamForOwnership(propertyOwnershipId, fileUploadId, certificateType)
        } else if (landlordId != null) {
            saveEmailForJourney(journeyId, fileUploadId, certificateType, landlordId)
            saveEmailToMonitoringTeam(journeyId, fileUploadId, certificateType, landlordId)
        }
    }

    // Re-points a submitted file's existing virus-scan callbacks from their in-progress journey target to the
    // registered property owner, updating each callback row in place rather than deleting and recreating it. The
    // update is set-based, so the scan-processor's concurrent per-row delete cannot make either side fail on a
    // zero-row write, and a file whose callbacks the scan has already processed simply has no rows to update (so no
    // orphaned callbacks are created).
    fun updateCallbacksToOwner(
        fileUploadId: Long,
        propertyOwnershipId: Long,
        certificateType: CertificateType,
    ) {
        virusScanCallbackRepository.findAllByFileUpload_Id(fileUploadId).forEach { callback ->
            val ownerData = EmailNotificationData.OwnerEmailNotification(propertyOwnershipId, certificateType)
            val updatedData =
                when (Json.decodeFromString<EmailNotificationData>(callback.encodedCallbackData)) {
                    is EmailNotificationData.VirusMonitoringEmailNotification ->
                        EmailNotificationData.VirusMonitoringEmailNotification(ownerData)

                    else -> ownerData
                }
            virusScanCallbackRepository.updateEncodedCallbackDataById(
                callback.id,
                Json.encodeToString<EmailNotificationData>(updatedData),
            )
        }
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
        val landlordId: Long,
    ) : EmailNotificationData()

    @Serializable
    data class VirusMonitoringEmailNotification(
        val internalEmailData: EmailNotificationData,
    ) : EmailNotificationData()
}
