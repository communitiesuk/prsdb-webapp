package uk.gov.communities.prsdb.webapp.services

import uk.gov.communities.prsdb.webapp.viewmodel.TestEmail

interface EmailNotificationService {
    fun sendTestEmail(
        recipientAddress: String,
        testEmail: TestEmail,
    )
}
