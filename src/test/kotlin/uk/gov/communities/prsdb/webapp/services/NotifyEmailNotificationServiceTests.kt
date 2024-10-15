package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import uk.gov.communities.prsdb.webapp.viewmodel.TestEmail
import uk.gov.service.notify.NotificationClient

class NotifyEmailNotificationServiceTests {
    private lateinit var notifyClient: NotificationClient
    private lateinit var emailNotificationService: NotifyEmailNotificationService<TestEmail>

    @BeforeEach
    fun setup() {
        notifyClient = Mockito.mock(NotificationClient::class.java)
        emailNotificationService = NotifyEmailNotificationService(notifyClient)
    }

    // This test currently tests that the test email hash map matches the corresponding template
    // TODO PRSD-364: When bringing templates into source control, test each template creates a hash map that matches the Notify template
    @Test
    fun `sendTestEmail sends a matching email using the notification client`() {
        // Arrange
        val email = TestEmail("Recipient")
        val recipientEmail = "an email address"

        // Act
        emailNotificationService.sendEmail(recipientEmail, email)

        // Assert
        Mockito
            .verify(
                notifyClient,
                Mockito.times(1),
            ).sendEmail(email.templateId.idValue, recipientEmail, hashMapOf("first name" to email.firstName), null)
    }
}
