package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.enums.CallbackType
import uk.gov.communities.prsdb.webapp.constants.enums.CertificateType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail

@PrsdbTaskService
class VirusCallbackHandler(
    private val emailNotificationService: EmailNotificationService<VirusScanUnsuccessfulEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    @Value("\${notify.support-email}") private val virusMonitoringEmail: String,
) {
    fun handleCallback(callback: VirusScanCallback) =
        when (callback.type) {
            CallbackType.SendEmailToOwner -> sendAlertToOwner(callback)
            CallbackType.SendVirusMonitoringEmail -> sendAlertToMonitoringTeam(callback)
        }

    private fun sendAlertToOwner(callback: VirusScanCallback) {
        val callbackData = Json.decodeFromString<OwnerEmailCallbackData>(callback.encodedCallbackData)
        val ownership = getPropertyOwnership(callbackData.propertyOwnershipId)

        val email = buildAlertEmail(ownership, callbackData.certificateType)
        emailNotificationService.sendEmail(ownership.primaryLandlord.email, email)
    }

    private fun sendAlertToMonitoringTeam(callback: VirusScanCallback) {
        val callbackData = Json.decodeFromString<OwnerEmailCallbackData>(callback.encodedCallbackData)
        val ownership = getPropertyOwnership(callbackData.propertyOwnershipId)

        val email = buildAlertEmail(ownership, callbackData.certificateType)
        emailNotificationService.sendEmail(virusMonitoringEmail, email)
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
