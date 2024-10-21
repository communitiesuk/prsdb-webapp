package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.models.viewModels.EmailTemplateModel

interface EmailNotificationService<in EmailModel : EmailTemplateModel> {
    fun sendEmail(
        recipientAddress: String,
        email: EmailModel,
    )
}
