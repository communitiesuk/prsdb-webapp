package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplate
import uk.gov.service.notify.NotificationClient

@Service
class NotifyEmailNotificationService<Template : EmailTemplate>(
    var notificationClient: NotificationClient,
) : EmailNotificationService<Template> {
    override fun sendEmail(
        recipientAddress: String,
        email: Template,
    ) {
        val emailParameters = email.asHashMap()
        notificationClient.sendEmail(email.templateId, recipientAddress, emailParameters, null)
    }
}
