package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
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

    @ParameterizedTest
    @MethodSource("getNotifyErrorMessages")
    fun `Correctly parses exception messages`(
        errorMessage: String,
        expectedError: NotifyErrorType,
    ) {
        val parsed = emailNotificationService.parseNotifyExceptionErrors(errorMessage)
        assertEquals(parsed, expectedError)
    }

    companion object {
        @JvmStatic
        fun getNotifyErrorMessages(): List<Arguments> =
            listOf(
                Arguments.of(
                    "Status code: 403 {" +
                        "\"errors\":[{\"error\":\"AuthError\",\"message\":\"Invalid token: service not found\"}]," +
                        "\"status_code\":403}",
                    NotifyErrorType.AUTH,
                ),
            )
    }
}
