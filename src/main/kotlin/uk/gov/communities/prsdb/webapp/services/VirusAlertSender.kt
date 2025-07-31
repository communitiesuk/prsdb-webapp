package uk.gov.communities.prsdb.webapp.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.dataModels.PropertyFileNameInfo.FileCategory
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.VirusScanUnsuccessfulEmail

@Service
class VirusAlertSender(
    private val emailNotificationService: EmailNotificationService<VirusScanUnsuccessfulEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
    @Value("\${notify.support-email}") private val virusMonitoringEmail: String,
) {
    fun sendAlerts(
        ownership: PropertyOwnership,
        category: FileCategory,
    ) {
        val email = buildAlertEmail(ownership, category)
        emailNotificationService.sendEmail(ownership.primaryLandlord.email, email)
        emailNotificationService.sendEmail(virusMonitoringEmail, email)
    }

    private fun buildAlertEmail(
        propertyOwnership: PropertyOwnership,
        fileCategory: FileCategory,
    ): VirusScanUnsuccessfulEmail =
        VirusScanUnsuccessfulEmail(
            certificateDescriptionForSubject(fileCategory),
            certificateDescriptionForHeading(fileCategory),
            certificateDescriptionForBody(fileCategory),
            propertyOwnership.property.address.singleLineAddress,
            RegistrationNumberDataModel.fromRegistrationNumber(propertyOwnership.registrationNumber).toString(),
            absoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id),
        )

    private fun certificateDescriptionForSubject(category: FileCategory): String =
        when (category) {
            FileCategory.GasSafetyCert -> "A gas safety certificate"
            FileCategory.Eirc -> "An EICR"
        }

    private fun certificateDescriptionForHeading(category: FileCategory): String =
        when (category) {
            FileCategory.GasSafetyCert -> "gas safety certificate"
            FileCategory.Eirc -> "Electrical Installation Condition Report (EICR)"
        }

    private fun certificateDescriptionForBody(category: FileCategory): String =
        when (category) {
            FileCategory.GasSafetyCert -> "gas safety certificate"
            FileCategory.Eirc -> "EICR"
        }
}
