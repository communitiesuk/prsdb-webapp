package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.FileUploadRepository
import uk.gov.communities.prsdb.webapp.database.repository.VirusScanCallbackRepository

@PrsdbWebService
class VirusScanCallbackService(
    private val virusScanCallbackRepository: VirusScanCallbackRepository,
    private val fileUploadRepository: FileUploadRepository,
    private val featureFlagManager: FeatureFlagManager,
) {
    fun saveEmailForJourney(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
        landlordId: Long,
    ): VirusScanCallback {
        val data =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
                landlordId = landlordId,
            )

        return saveVirusScanCallback(fileUploadId, data)
    }

    fun saveEmailToMonitoringTeam(
        journeyId: String,
        fileUploadId: Long,
        certificateType: CertificateType,
        landlordId: Long,
    ): VirusScanCallback {
        val internalData =
            EmailNotificationData.IncompletePropertyEmailNotification(
                journeyId = journeyId,
                certificateType = certificateType,
                landlordId = landlordId,
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
        landlordId: Long?,
    ) {
        // TODO: PDJB-1617: Remove feature flag check when we remove the DELEGATE_TO_LETTING_AGENT flag
        if (propertyOwnershipId != null && featureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)) {
            saveEmailForUpdateJourney(propertyOwnershipId, fileUploadId, certificateType)
            saveEmailToMonitoringTeamForUpdateJourney(propertyOwnershipId, fileUploadId, certificateType)
        } else if (landlordId != null) {
            saveEmailForJourney(journeyId, fileUploadId, certificateType, landlordId)
            saveEmailToMonitoringTeam(journeyId, fileUploadId, certificateType, landlordId)
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
        val landlordId: Long,
    ) : EmailNotificationData()

    @Serializable
    data class VirusMonitoringEmailNotification(
        val internalEmailData: EmailNotificationData,
    ) : EmailNotificationData()
}
