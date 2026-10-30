package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASYNCHRONOUS_EMAIL_SENDING
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel

@Service
class RoutingEmailNotificationService<EmailModel : EmailTemplateModel>(
    private val emailDeliveryService: EmailDeliveryService,
    private val emailQueueService: EmailQueueService,
    private val featureFlagManager: FeatureFlagManager,
) : EmailNotificationService<EmailModel> {
    override fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) {
        if (featureFlagManager.checkFeature(ASYNCHRONOUS_EMAIL_SENDING)) {
            emailQueueService.enqueue(recipientAddress, email)
        } else {
            emailDeliveryService.deliverEmail(recipientAddress, email)
        }
    }
}
