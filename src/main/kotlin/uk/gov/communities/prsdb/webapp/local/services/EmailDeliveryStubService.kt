package uk.gov.communities.prsdb.webapp.local.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel
import uk.gov.communities.prsdb.webapp.services.EmailDeliveryService

@Service
// The one-login-simulator profile is only active on NFT during performance runs, so no real emails are sent
@Profile("(local & !use-notify) | one-login-simulator")
@Primary
class EmailDeliveryStubService : EmailDeliveryService {
    @Value("\${local.emails.transientExceptionAddress:}")
    var transientExceptionRecipient: String? = null

    @Value("\${local.emails.persistentExceptionAddress:}")
    var persistentExceptionRecipient: String? = null

    override fun deliverEmail(
        recipientAddress: String,
        email: EmailTemplateModel,
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
            Template used:   ${email.template.name}
            Personalisation: ${email.toHashMap()}
            """.trimIndent()

        println(consoleString)
    }
}
