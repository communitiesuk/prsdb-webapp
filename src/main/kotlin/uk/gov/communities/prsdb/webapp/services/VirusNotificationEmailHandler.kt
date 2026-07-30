package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.IndividualLandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationSingleLineAddress
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationData.IncompletePropertyEmailNotification
import uk.gov.communities.prsdb.webapp.services.EmailNotificationData.OwnerEmailNotification
import uk.gov.communities.prsdb.webapp.services.EmailNotificationData.VirusMonitoringEmailNotification

@PrsdbTaskService
class VirusNotificationEmailHandler(
    private val emailNotificationService: EmailNotificationService<EmailTemplateModel>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    private val individualLandlordRepository: IndividualLandlordRepository,
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
    @Value("\${notify.support-email}") private val virusMonitoringEmail: String,
) {
    fun handleCallback(callback: VirusScanCallback) =
        when (val callbackData = Json.decodeFromString<EmailNotificationData>(callback.encodedCallbackData)) {
            is OwnerEmailNotification -> sendAlertToOwners(callbackData)

            is VirusMonitoringEmailNotification -> sendAlertToMonitoringTeam(callbackData)

            is IncompletePropertyEmailNotification -> sendAlertForIncompleteProperty(callbackData)
        }

    private fun sendAlertToOwners(
        notification: OwnerEmailNotification,
        monitoringEmailAddress: String? = null,
    ) {
        val ownership = getPropertyOwnership(notification.propertyOwnershipId)

        if (monitoringEmailAddress != null) {
            emailNotificationService.sendEmail(
                monitoringEmailAddress,
                buildAlertEmail(notification.certificateType, MONITORING_TEAM_RECIPIENT_NAME, ownership.address.singleLineAddress),
            )
        } else {
            // TODO: PDJB-1274: Update emails to account for org landlord
            ownership.landlords.forEach { landlord ->
                check(landlord is IndividualLandlord)
                emailNotificationService.sendEmail(
                    landlord.email,
                    buildAlertEmail(notification.certificateType, landlord.name, ownership.address.singleLineAddress),
                )
            }
        }
    }

    private fun sendAlertForIncompleteProperty(
        notification: IncompletePropertyEmailNotification,
        monitoringEmailAddress: String? = null,
    ) {
        val landlord =
            individualLandlordRepository.findById(notification.landlordId).orElse(null)
                ?: throw IllegalStateException("No individual landlord found for id: ${notification.landlordId}")
        val savedJourneyState =
            savedJourneyStateRepository.findByJourneyIdAndUser_Id(notification.journeyId, landlord.baseUser.id)
                ?: throw IllegalStateException("No saved journey state found for journeyId: ${notification.journeyId}")

        // TODO: PDJB-1274: update to account for org landlords
        emailNotificationService.sendEmail(
            monitoringEmailAddress ?: landlord.email,
            buildAlertEmail(
                notification.certificateType,
                if (monitoringEmailAddress != null) MONITORING_TEAM_RECIPIENT_NAME else landlord.name,
                savedJourneyState.getPropertyRegistrationSingleLineAddress(),
            ),
        )
    }

    private fun sendAlertToMonitoringTeam(notification: VirusMonitoringEmailNotification) =
        when (val internalNotification = notification.internalEmailData) {
            is OwnerEmailNotification -> {
                sendAlertToOwners(internalNotification, virusMonitoringEmail)
            }

            is IncompletePropertyEmailNotification -> {
                sendAlertForIncompleteProperty(internalNotification, virusMonitoringEmail)
            }

            is VirusMonitoringEmailNotification -> {
                throw IllegalStateException("A monitoring email cannot be about a monitoring email")
            }
        }

    private fun getPropertyOwnership(id: Long): PropertyOwnership =
        propertyOwnershipRepository.findByIdAndIsActiveTrue(id)
            ?: throw IllegalStateException("No active property ownership found for id: $id")

    private fun buildAlertEmail(
        certificateType: CertificateType,
        recipientName: String,
        singleLineAddress: String,
    ): VirusScanUnsuccessfulEmail =
        VirusScanUnsuccessfulEmail(
            certificateType = certificateDescriptionForBody(certificateType),
            recipientName = recipientName,
            propertyAddress = singleLineAddress,
            landlordDashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
        )

    private fun certificateDescriptionForBody(category: CertificateType): String =
        when (category) {
            CertificateType.GasSafetyCert -> "gas safety certificate"
            CertificateType.Eicr -> "EICR"
            CertificateType.Eic -> "EIC"
        }

    companion object {
        private const val MONITORING_TEAM_RECIPIENT_NAME = "Monitoring Team"
    }
}
