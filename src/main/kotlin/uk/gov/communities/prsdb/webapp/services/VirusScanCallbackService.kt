package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository

@PrsdbWebService
class VirusScanCallbackService(
    private val virusScanCallbackRepository: VirusScanCallbackRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val userToLandlordService: UserToLandlordService,
) {
    fun saveEmailForJourney(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
        landlordId: Long,
        userId: String,
    ): VirusScanCallback {
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
                landlordId = landlordId,
                userId = userId,
            )

        return saveVirusScanCallback(fileUploadId, data)
    }

    fun saveEmailToMonitoringTeam(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
        landlordId: Long,
        userId: String,
    ): VirusScanCallback {
        val internalData =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
                landlordId = landlordId,
                userId = userId,
            )
        val data = EmailNotificationData.VirusMonitoringEmailNotification(internalData)

        return saveVirusScanCallback(fileUploadId, data)
    }

    fun saveEmailForUpdateJourney(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val data = EmailNotificationData.OwnerEmailNotification(propertyOwnershipId, certificateType)

        return saveVirusScanCallback(fileUploadId, data)
    }

    fun saveEmailToMonitoringTeamForUpdateJourney(
        propertyOwnershipId: Long,
        fileUploadId: Long,
        certificateType: CertificateType,
    ): VirusScanCallback {
        val internalData = EmailNotificationData.OwnerEmailNotification(propertyOwnershipId, certificateType)
        val data = EmailNotificationData.VirusMonitoringEmailNotification(internalData)

        return saveVirusScanCallback(fileUploadId, data)
    }

    private fun saveVirusScanCallback(
        fileUploadId: Long,
        data: EmailNotificationData,
    ): VirusScanCallback {
        val fileUpload = fileUploadRepository.getReferenceById(fileUploadId)

        return virusScanCallbackRepository.save(
            VirusScanCallback(
                upload = fileUpload,
                encodedCallbackData = Json.encodeToString<EmailNotificationData>(data),
            ),
        )
    }

    fun saveVirusScanFailureEmail(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
        propertyOwnershipId: Long?,
    ) {
        if (propertyOwnershipId != null) {
            saveEmailForUpdateJourney(propertyOwnershipId, fileUploadId, certificateType)
            saveEmailToMonitoringTeamForUpdateJourney(propertyOwnershipId, fileUploadId, certificateType)
        } else {
            val landlordId = userToLandlordService.getCurrentLandlordForUser().id
            val userId = SecurityContextHolder.getContext().authentication!!.name
            saveEmailForJourney(journeyId, fileUploadId, certificateType, landlordId, userId)
            saveEmailToMonitoringTeam(journeyId, fileUploadId, certificateType, landlordId, userId)
        }
    }

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
        // TODO PDJB-1919: Remove landlordId once all saved notifications have a userId
        val landlordId: Long,
        val userId: String? = null,
    ) : EmailNotificationData()

    @Serializable
    data class VirusMonitoringEmailNotification(
        val internalEmailData: EmailNotificationData,
    ) : EmailNotificationData()
}
