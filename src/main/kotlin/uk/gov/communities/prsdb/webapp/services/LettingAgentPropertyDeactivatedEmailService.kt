package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.LettingAgentPropertyDeactivatedNotificationEmail

@Service
class LettingAgentPropertyDeactivatedEmailService(
    private val emailNotificationService: EmailNotificationService<LettingAgentPropertyDeactivatedNotificationEmail>,
) {
    fun sendLettingAgentPropertyDeactivatedEmail(
        propertyOwnership: PropertyOwnership,
        lettingAgentEmail: String,
    ) {
        emailNotificationService.sendEmail(
            lettingAgentEmail,
            LettingAgentPropertyDeactivatedNotificationEmail(
                propertyAddress = propertyOwnership.address.toMultiLineAddress(),
                singleLineAddress = propertyOwnership.address.singleLineAddress,
            ),
        )
    }
}
