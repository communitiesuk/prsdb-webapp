package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplate

interface EmailNotificationService<Template : EmailTemplate> {
    fun sendEmail(
        recipientAddress: String,
        email: Template,
    )
}
