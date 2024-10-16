package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateId
import uk.gov.communities.prsdb.webapp.viewmodel.EmailTemplateModel
import uk.gov.service.notify.NotificationClient

class NotifyEmailNotificationServiceTests {
    private lateinit var notifyClient: NotificationClient
    private lateinit var emailNotificationService: NotifyEmailNotificationService<TestEmailTemplate>

    @BeforeEach
    fun setup() {
        notifyClient = Mockito.mock(NotificationClient::class.java)
        emailNotificationService = NotifyEmailNotificationService(notifyClient)
    }

    private class TestEmailTemplate(
        val hashMap: HashMap<String, String>,
        override val templateId: EmailTemplateId,
    ) : EmailTemplateModel {
        override fun toHashMap(): HashMap<String, String> = hashMap
    }

    @Test
    fun `sendEmail sends a matching email using the notification client`() {
        // Arrange
        val expectedHashmap = hashMapOf("test key 1" to "test value", "test key 2" to "test value")
        val expectedTemplateId = EmailTemplateId.EXAMPLE_EMAIL
        val email = TestEmailTemplate(expectedHashmap, expectedTemplateId)
        val recipientEmail = "an email address"

        // Act
        emailNotificationService.sendEmail(recipientEmail, email)

        // Assert
        Mockito
            .verify(
                notifyClient,
                Mockito.times(1),
            ).sendEmail(expectedTemplateId.idValue, recipientEmail, expectedHashmap, null)
    }
}
