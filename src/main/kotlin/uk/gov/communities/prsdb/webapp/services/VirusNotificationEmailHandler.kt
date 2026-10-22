package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.IndividualLandlord
import uk.gov.communities.prsdb.webapp.database.entity.OrganisationalLandlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.LandlordRepository
import uk.gov.communities.prsdb.webapp.database.repository.LettingAgentAccessRepository
import uk.gov.communities.prsdb.webapp.database.repository.OrganisationalLandlordUserRepository
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.database.repository.SavedJourneyStateRepository
import uk.gov.communities.prsdb.webapp.helpers.extensions.savedJourneyStateExtensions.SavedJourneyStateExtensions.Companion.getPropertyRegistrationMultiLineAddress
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
    private val landlordRepository: LandlordRepository,
    private val organisationalLandlordUserRepository: OrganisationalLandlordUserRepository,
    private val savedJourneyStateRepository: SavedJourneyStateRepository,
    private val lettingAgentAccessRepository: LettingAgentAccessRepository,
    private val landlordUserEmailService: LandlordUserEmailService,
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
                buildAlertEmail(notification.certificateType, MONITORING_TEAM_RECIPIENT_NAME, ownership.address.toMultiLineAddress()),
            )
        } else {
            ownership.landlords.forEach { landlord ->
                emailNotificationService.sendEmailToLandlord(
                    landlord,
                    buildAlertEmail(notification.certificateType, landlord.name, ownership.address.toMultiLineAddress()),
                )
            }

            val lettingAgentAccess = lettingAgentAccessRepository.findByPropertyOwnershipId(ownership.id)
            if (PropertyOwnershipService.hasLettingAgent(ownership, lettingAgentAccess)) {
                emailNotificationService.sendEmail(
                    lettingAgentAccess!!.invitedEmail,
                    buildAlertEmail(
                        notification.certificateType,
                        lettingAgentAccess.invitedEmail,
                        ownership.address.toMultiLineAddress(),
                    ),
                )
            }
        }
    }

    private fun sendAlertForIncompleteProperty(
        notification: IncompletePropertyEmailNotification,
        monitoringEmailAddress: String? = null,
    ) {
        val savedJourneyState =
            findSavedJourneyState(notification)
                ?: throw IllegalStateException("No saved journey state found for journeyId: ${notification.journeyId}")

        if (monitoringEmailAddress != null) {
            val email =
                buildAlertEmail(
                    notification.certificateType,
                    MONITORING_TEAM_RECIPIENT_NAME,
                    savedJourneyState.getPropertyRegistrationMultiLineAddress(),
                )
            emailNotificationService.sendEmail(monitoringEmailAddress, email)
        } else {
            val userDetails = landlordUserEmailService.getUserDetailsByBaseUserId(savedJourneyState.user.id)
            val email =
                buildAlertEmail(
                    notification.certificateType,
                    userDetails.name,
                    savedJourneyState.getPropertyRegistrationMultiLineAddress(),
                )
            emailNotificationService.sendEmail(userDetails.email, email)
        }
    }

    private fun findSavedJourneyState(notification: IncompletePropertyEmailNotification): SavedJourneyState? {
        // TODO PDJB-1919: The findByJourneyIdAndUser_Id call can be inlined as notification.userId != null will always be true
        // then we can remove findSavedJourneyState
        if (notification.userId != null) {
            return savedJourneyStateRepository.findByJourneyIdAndUser_Id(notification.journeyId, notification.userId)
        }

        // TODO PDJB-1919: Remove this fallback code (the rest of this method) once all saved notifications have a userId
        val landlord =
            landlordRepository.findById(notification.landlordId).orElse(null)
                ?: throw IllegalStateException("No landlord found for id: ${notification.landlordId}")
        val baseUserIds =
            when (landlord) {
                is IndividualLandlord -> {
                    listOf(landlord.baseUser.id)
                }

                is OrganisationalLandlord -> {
                    organisationalLandlordUserRepository.findByOrganisationalLandlord(landlord).map { it.baseUser.id }
                }

                else -> {
                    throw IllegalStateException("Unexpected landlord type for id ${landlord.id}: ${landlord.landlordType}")
                }
            }
        return savedJourneyStateRepository.findFirstByJourneyIdAndUser_IdIn(notification.journeyId, baseUserIds)
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
        multiLineAddress: String,
    ): VirusScanUnsuccessfulEmail =
        VirusScanUnsuccessfulEmail(
            certificateType = certificateDescriptionForBody(certificateType),
            // TODO PDJB-1701: Remove recipientName and landlordDashboardUrl once Notify template V3 is live
            recipientName = recipientName,
            propertyAddress = multiLineAddress,
            landlordDashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri(),
        )

    private fun certificateDescriptionForBody(category: CertificateType): String =
        when (category) {
            CertificateType.GasSafetyCert -> "Gas safety certificate"
            CertificateType.Eicr -> "EICR"
            CertificateType.Eic -> "EIC"
        }

    companion object {
        private const val MONITORING_TEAM_RECIPIENT_NAME = "Monitoring Team"
    }
}
