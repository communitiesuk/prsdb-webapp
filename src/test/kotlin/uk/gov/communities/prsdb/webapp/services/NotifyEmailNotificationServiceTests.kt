package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.emailModels.EmailTemplateId
import uk.gov.communities.prsdb.webapp.models.emailModels.EmailTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

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

        constructor() : this(hashMapOf(), EmailTemplateId.EXAMPLE_EMAIL)
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
    @MethodSource("getIndefiniteProblemExceptionMessages")
    fun `Throws CannotSendEmailsException when the service is unable to send emails`(errorMessage: String) {
        // Arrange
        var innerException = NotificationClientException(errorMessage)
        Mockito
            .doThrow(innerException)
            .whenever(notifyClient)
            .sendEmail(any(), any(), any(), any())

        var thrownException =
            assertThrows<PersistentEmailSendException> { emailNotificationService.sendEmail("", TestEmailTemplate()) }

        assertEquals(thrownException.cause, innerException)
    }

    @ParameterizedTest
    @MethodSource("getTemporaryProblemExceptionMessages")
    fun `Throws EmailWasNotSentException when the service failed to send that email`(error: String) {
        // Arrange
        var innerException = NotificationClientException(error)
        Mockito
            .doThrow(innerException)
            .whenever(notifyClient)
            .sendEmail(any(), any(), any(), any())

        var thrownException =
            assertThrows<TransientEmailSentException> { emailNotificationService.sendEmail("", TestEmailTemplate()) }

        assertEquals(thrownException.cause, innerException)
    }

    companion object {
        @JvmStatic
        fun getTemporaryProblemExceptionMessages(): List<Named<String>> =
            listOf(
                named(
                    "Rate Limit Error",
                    "Status code: 429 {" +
                        "\"errors\":[{\"error\":\"RateLimitError\",\"message\":\"Exceeded rate limit for key type\"}]," +
                        "\"status_code\":429}",
                ),
                named(
                    "Exception",
                    "Status code: 500 {" +
                        "\"errors\":[{\"error\":\"Exception\",\"message\":\"Internal server error\"}]," +
                        "\"status_code\":500}",
                ),
                named(
                    "Multiple transient errors",
                    "Status code: 500 {" +
                        "\"errors\":[" +
                        "{\"error\":\"Exception\",\"message\":\"Internal server error\"}," +
                        "{\"error\":\"RateLimitError\",\"message\":\"Exceeded rate limit for key type\"}]," +
                        "\"status_code\":500}",
                ),
            )

        @JvmStatic
        fun getIndefiniteProblemExceptionMessages(): List<Named<String>> =
            listOf(
                named(
                    "Auth Error",
                    "Status code: 403 {" +
                        "\"errors\":[{\"error\":\"AuthError\",\"message\":\"Invalid token: service not found\"}]," +
                        "\"status_code\":403}",
                ),
                named(
                    "Bad Request Error",
                    "Status code: 400 {" +
                        "\"errors\":[{\"error\":\"BadRequestError\",\"message\":\"Cant send to this recipient using a this API key\"}]," +
                        "\"status_code\":400}",
                ),
                named(
                    "Too Many Requests Error",
                    "Status code: 429 {" +
                        "\"errors\":[{\"error\":\"TooManyRequestsError\",\"message\":\"Exceeded send limits for today\"}]," +
                        "\"status_code\":429}",
                ),
                named(
                    "Multiple permanent errors",
                    "Status code: 429 {" +
                        "\"errors\":[" +
                        "{\"error\":\"TooManyRequestsError\",\"message\":\"Exceeded send limits for today\"}," +
                        "{\"error\":\"AuthError\",\"message\":\"Invalid token: service not found\"}]," +
                        "\"status_code\":429}",
                ),
                named(
                    "Transient and permanent errors",
                    "Status code: 429 {" +
                        "\"errors\":[" +
                        "{\"error\":\"TooManyRequestsError\",\"message\":\"Exceeded send limits for today\"}," +
                        "{\"error\":\"Exception\",\"message\":\"Internal server error\"}]," +
                        "\"status_code\":429}",
                ),
            )
    }
}
