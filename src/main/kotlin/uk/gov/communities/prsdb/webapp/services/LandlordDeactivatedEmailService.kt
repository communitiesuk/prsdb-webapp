package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.constants.enums.LandlordDeactivationReason
import uk.gov.communities.prsdb.webapp.database.entity.Landlord
import uk.gov.communities.prsdb.webapp.helpers.NotificationHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LandlordDeactivatedEmail

@Service
class LandlordDeactivatedEmailService(
    private val landlordDeactivatedEmailSender: EmailNotificationService<LandlordDeactivatedEmail>,
    private val absoluteUrlProvider: AbsoluteUrlProvider,
) {
    fun sendLandlordDeactivatedEmail(
        landlord: Landlord,
        reason: LandlordDeactivationReason,
    ) {
        NotificationHelper.sendNotificationAndLogFailures("the landlord deactivated email for landlord ${landlord.id}") {
            landlordDeactivatedEmailSender.sendEmailToLandlord(
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
}
