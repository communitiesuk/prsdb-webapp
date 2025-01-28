package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService

@Service
@Profile("local & !use-notify")
@Primary
class EmailNotificationStubService<EmailModel : EmailTemplateModel> : EmailNotificationService<EmailModel> {
    @Value("\${local.emails.transientExceptionAddress}")
    var transientExceptionRecipient: String? = null

    @Value("\${local.emails.persistentExceptionAddress}")
    var persistentExceptionRecipient: String? = null

    override fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    ) {
        if (recipientAddress == persistentExceptionRecipient) {
            throw PersistentEmailSendException("Emails sent to $persistentExceptionRecipient always persistently fail")
        }
        if (recipientAddress == transientExceptionRecipient) {
            throw TransientEmailSentException("Emails sent to $transientExceptionRecipient always transiently fail")
        }

        val consoleString =
            """
            ***************
            STUB EMAIL SENT
            ***************
            
            Email sent to:   $recipientAddress
            Template used:   ${email.templateId.name}
            Personalisation: ${email.toHashMap()}
            """.trimIndent()

        println(consoleString)
    }
}
