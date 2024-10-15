package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateModel

interface EmailNotificationService<EmailModel : EmailTemplateModel> {
    fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    )
}
