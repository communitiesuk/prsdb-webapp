package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationData.IncompletePropertyEmailNotification
import uk.gov.communities.prsdb.webapp.services.EmailNotificationData.OwnerEmailNotification
import uk.gov.communities.prsdb.webapp.services.EmailNotificationData.VirusMonitoringEmailNotification

@PrsdbTaskService
class VirusNotificationEmailHandler(
    private val emailNotificationService: EmailNotificationService<VirusScanUnsuccessfulEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    @Value("\${notify.support-email}") private val virusMonitoringEmail: String,
) {
    fun handleCallback(callback: VirusScanCallback) =
        when (val callbackData = Json.decodeFromString<EmailNotificationData>(callback.encodedCallbackData)) {
            is OwnerEmailNotification -> sendAlertToOwner(callbackData)
            is VirusMonitoringEmailNotification -> sendAlertToMonitoringTeam(callbackData)
            // TODO PDJB-717: Handle notifying the user and the monitoring team for incomplete journeys
            is IncompletePropertyEmailNotification -> TODO("PDJB-717")
        }

    private fun sendAlertToOwner(
        notification: OwnerEmailNotification,
        emailAddress: String? = null,
    ) {
        val ownership = getPropertyOwnership(notification.propertyOwnershipId)

        val email = buildAlertEmail(ownership, notification.certificateType)
        emailNotificationService.sendEmail(emailAddress ?: ownership.primaryLandlord.email, email)
    }

    private fun sendAlertToMonitoringTeam(notification: VirusMonitoringEmailNotification) =
        when (val internalNotification = notification.internalEmailData) {
            is OwnerEmailNotification -> sendAlertToOwner(internalNotification, virusMonitoringEmail)
            is IncompletePropertyEmailNotification -> TODO("PDJB-717")
            is VirusMonitoringEmailNotification ->
                throw IllegalStateException("A monitoring email cannot be about a monitoring email")
        }

    private fun getPropertyOwnership(id: Long): PropertyOwnership =
        propertyOwnershipRepository.findByIdAndIsActiveTrue(id)
            ?: throw IllegalStateException("No active property ownership found for id: $id")

    private fun buildAlertEmail(
        propertyOwnership: PropertyOwnership,
        certificateType: CertificateType,
    ): VirusScanUnsuccessfulEmail =
        VirusScanUnsuccessfulEmail(
            certificateDescriptionForSubject(certificateType),
            certificateDescriptionForHeading(certificateType),
            certificateDescriptionForBody(certificateType),
            propertyOwnership.address.singleLineAddress,
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
            absoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id),
        )

    private fun certificateDescriptionForSubject(certificateType: CertificateType): String =
        when (certificateType) {
            CertificateType.GasSafetyCert -> "A gas safety certificate"
            CertificateType.Eicr -> "An EICR"
        }

    private fun certificateDescriptionForHeading(certificateType: CertificateType): String =
        when (certificateType) {
            CertificateType.GasSafetyCert -> "gas safety certificate"
            CertificateType.Eicr -> "Electrical Installation Condition Report (EICR)"
        }

    private fun certificateDescriptionForBody(category: CertificateType): String =
        when (category) {
            CertificateType.GasSafetyCert -> "gas safety certificate"
            CertificateType.Eicr -> "EICR"
        }
}
