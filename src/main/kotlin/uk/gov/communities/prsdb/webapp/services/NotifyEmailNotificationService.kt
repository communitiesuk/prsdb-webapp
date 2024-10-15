package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateModel
import uk.gov.service.notify.NotificationClient

@Service
class NotifyEmailNotificationService<EmailModel : EmailTemplateModel>(
    var notificationClient: NotificationClient,
) : EmailNotificationService<EmailModel> {
    override fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) {
        val emailParameters = email.toHashMap()
        notificationClient.sendEmail(email.templateId, recipientAddress, emailParameters, null)
    }
}
