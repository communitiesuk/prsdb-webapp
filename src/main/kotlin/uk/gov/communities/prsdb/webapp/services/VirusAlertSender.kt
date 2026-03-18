package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.constants.enums.CallbackType
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.VirusScanCallback
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail

@PrsdbTaskService
class VirusAlertSender(
    private val emailNotificationService: EmailNotificationService<VirusScanUnsuccessfulEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    private val propertyOwnershipRepository: PropertyOwnershipRepository,
    @Value("\${notify.support-email}") private val virusMonitoringEmail: String,
) {
    fun sendAlerts(callback: VirusScanCallback) {
        val ownership =
            propertyOwnershipRepository.findByIdAndIsActiveTrue(callback.encodedCallbackData.toLong())
                ?: throw IllegalStateException("No active property ownership found for id: ${callback.encodedCallbackData}")

        val email = buildAlertEmail(ownership, callback.type)
        emailNotificationService.sendEmail(ownership.primaryLandlord.email, email)
        emailNotificationService.sendEmail(virusMonitoringEmail, email)
    }

    private fun buildAlertEmail(
        propertyOwnership: PropertyOwnership,
        callbackType: CallbackType,
    ): VirusScanUnsuccessfulEmail =
        VirusScanUnsuccessfulEmail(
            certificateDescriptionForSubject(callbackType),
            certificateDescriptionForHeading(callbackType),
            certificateDescriptionForBody(callbackType),
            propertyOwnership.address.singleLineAddress,
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
            absoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id),
        )

    private fun certificateDescriptionForSubject(category: CallbackType): String =
        when (category) {
            CallbackType.GasSafetyCert -> "A gas safety certificate"
            CallbackType.Eicr -> "An EICR"
        }

    private fun certificateDescriptionForHeading(category: CallbackType): String =
        when (category) {
            CallbackType.GasSafetyCert -> "gas safety certificate"
            CallbackType.Eicr -> "Electrical Installation Condition Report (EICR)"
        }

    private fun certificateDescriptionForBody(category: CallbackType): String =
        when (category) {
            CallbackType.GasSafetyCert -> "gas safety certificate"
            CallbackType.Eicr -> "EICR"
        }
}
