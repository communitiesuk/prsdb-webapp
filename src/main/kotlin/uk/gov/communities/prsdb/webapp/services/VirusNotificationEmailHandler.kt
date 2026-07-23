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
        emailAddress: String? = null,
    ) {
        val ownership = getPropertyOwnership(notification.propertyOwnershipId)

        if (emailAddress != null) {
            val firstLandlord = ownership.landlords.first()
            check(firstLandlord is IndividualLandlord)
            emailNotificationService.sendEmail(
                emailAddress,
                buildAlertEmail(ownership, notification.certificateType, firstLandlord.name),
            )
        } else {
            // TODO: PDJB-1274: Update emails to account for org landlord
            ownership.landlords.forEach { landlord ->
                check(landlord is IndividualLandlord)
                emailNotificationService.sendEmail(landlord.email, buildAlertEmail(ownership, notification.certificateType, landlord.name))
            }
        }
    }

    private fun sendAlertForIncompleteProperty(
        notification: IncompletePropertyEmailNotification,
        emailAddress: String? = null,
    ) {
        val landlord =
            individualLandlordRepository.findByBaseUser_Id(notification.subjectIdentifier)
                ?: throw IllegalStateException(
                    "No individual landlord found for subject identifier: ${notification.subjectIdentifier}",
                )
        val savedJourneyState =
            savedJourneyStateRepository.findByJourneyIdAndUser_Id(notification.journeyId, notification.subjectIdentifier)
                ?: throw IllegalStateException("No saved journey state found for journeyId: ${notification.journeyId}")

        val email =
            VirusScanUnsuccessfulEmail(
                certificateType = certificateDescriptionForBody(notification.certificateType),
                recipientName = landlord.name,
                propertyAddress = savedJourneyState.getPropertyRegistrationSingleLineAddress(),
                registerRentalPropertyURL = absoluteUrlProvider.buildLandlordDashboardUri(),
            )

        emailNotificationService.sendEmail(emailAddress ?: landlord.email, email)
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
        propertyOwnership: PropertyOwnership,
        certificateType: CertificateType,
        recipientName: String,
    ): VirusScanUnsuccessfulEmail =
        VirusScanUnsuccessfulEmail(
            certificateType = certificateDescriptionForBody(certificateType),
            recipientName = recipientName,
            propertyAddress = propertyOwnership.address.singleLineAddress,
            registerRentalPropertyURL = absoluteUrlProvider.buildLandlordDashboardUri(),
        )

    private fun certificateDescriptionForBody(category: CertificateType): String =
        when (category) {
            CertificateType.GasSafetyCert -> "gas safety certificate"
            CertificateType.Eicr -> "EICR"
            CertificateType.Eic -> "EIC"
        }
}
