package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordDeactivatedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LettingAgentPropertyDeactivatedNotificationEmail

@Service
class DeactivationEmailService(
    private val lettingAgentEmailSender: EmailNotificationService<LettingAgentPropertyDeactivatedNotificationEmail>,
    private val landlordEmailSender: EmailNotificationService<LandlordDeactivatedEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    fun sendLandlordDeactivatedEmail(
        landlord: Landlord,
        reason: LandlordDeactivationReason,
    ) {
        NotificationHelper.sendNotificationAndLogFailures("the landlord deactivated email for landlord ${landlord.id}") {
            landlordEmailSender.sendEmailToLandlord(
                landlord,
                LandlordDeactivatedEmail(
                    recipientName = landlord.name,
                    registrationNumber = RegistrationNumberDataModel.fromRegistrationNumber(landlord.registrationNumber).toString(),
                    reason = reason,
                    dashboardUrl = absoluteUrlProvider.buildLandlordDashboardUri().toString(),
                ),
            )
        }
    }

    fun sendLettingAgentPropertyDeactivatedEmail(
        propertyOwnership: PropertyOwnership,
        lettingAgentEmail: String,
    ) {
        NotificationHelper.sendNotificationAndLogFailures(
            "the letting agent property deactivated email for property ownership ${propertyOwnership.id}",
        ) {
            lettingAgentEmailSender.sendEmail(
                lettingAgentEmail,
                LettingAgentPropertyDeactivatedNotificationEmail(
                    propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                    singleLineAddress = propertyOwnership.address.singleLineAddress,
                ),
            )
        }
    }
}
