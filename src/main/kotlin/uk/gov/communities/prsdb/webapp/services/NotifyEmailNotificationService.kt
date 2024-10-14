package uk.gov.communities.prsdb.webapp.services

import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.viewmodel.TestEmail
import uk.gov.service.notify.NotificationClient

@Service
class NotifyEmailNotificationService(
    var notificationClient: NotificationClient,
) : EmailNotificationService {
    override fun sendTestEmail(
        recipientAddress: String,
        testEmail: TestEmail,
    ) {
        val emailParameters = testEmail.asHashMap()
        notificationClient.sendEmail(testEmail.templateId, recipientAddress, emailParameters, null)
    }
}
