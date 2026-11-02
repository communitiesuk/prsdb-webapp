package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel

@Service
class RoutingEmailNotificationService<EmailModel : EmailTemplateModel>(
    private val emailDeliveryService: EmailDeliveryService,
) : EmailNotificationService<EmailModel> {
    override fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) = emailDeliveryService.deliverEmail(recipientAddress, email)
}
