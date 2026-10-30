package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.Named.named
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PersistentLetterSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientLetterSendException
import uk.gov.communities.prsdb.webapp.models.dataModels.LetterAddressDataModel
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.letterModels.LetterTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException

class NotifyLetterNotificationServiceTests {
    private lateinit var notifyClient: NotificationClient
    private lateinit var notifyIdService: NotifyIdService
    private lateinit var letterNotificationService: NotifyLetterNotificationService<TestLetterTemplate>

    private val recipientAddress = LetterAddressDataModel(listOf("The Occupier", "10 High Street", "SW1A 1AA"))

    @BeforeEach
    fun setup() {
        notifyClient = mock()
        notifyIdService = mock()

        letterNotificationService = NotifyLetterNotificationService(notifyClient, notifyIdService)
    }

    private class TestLetterTemplate(
        val hashMap: HashMap<String, String>,
        override val template: LetterTemplate,
    ) : LetterTemplateModel {
        override fun toHashMap(): HashMap<String, String> = hashMap

        constructor() : this(hashMapOf(), LetterTemplate.EXAMPLE_LETTER)
    }

    @Test
    fun `sendLetter sends a matching letter using the notification client`() {
        // Arrange
        val letter = TestLetterTemplate(hashMapOf("heading" to "Example heading"), LetterTemplate.EXAMPLE_LETTER)
        val expectedNotifyIdValue = "some id value"

        whenever(notifyIdService.getNotifyIdValue(LetterTemplate.EXAMPLE_LETTER)).thenReturn(expectedNotifyIdValue)

        // Act
        letterNotificationService.sendLetter(recipientAddress, letter)

        // Assert
        Mockito
            .verify(
                notifyClient,
                Mockito.times(1),
            ).sendLetter(
                expectedNotifyIdValue,
                mapOf(
                    "heading" to "Example heading",
                    "address_line_1" to "The Occupier",
                    "address_line_2" to "10 High Street",
                    "address_line_3" to "SW1A 1AA",
                ),
                null,
            )
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "address_line_1",
            "address_line_7",
            "postcode",
            "Address_Line_1",
            "address line 2",
            "addressline3",
            "address-line-4",
            "Postcode",
            "post-code",
            "POST CODE",
        ],
    )
    fun `sendLetter rejects letter personalisation that would replace or extend the recipient address`(key: String) {
        // Arrange
        val letter = TestLetterTemplate(hashMapOf(key to "Another address"), LetterTemplate.EXAMPLE_LETTER)

        // Act & Assert
        assertThrows<IllegalArgumentException> { letterNotificationService.sendLetter(recipientAddress, letter) }
        Mockito.verifyNoInteractions(notifyClient)
    }

    @ParameterizedTest
    @ValueSource(strings = ["address", "postcode area", "address line", "property address line 1", "postcode_1"])
    fun `sendLetter allows letter personalisation keys that only resemble the recipient address keys`(key: String) {
        // Arrange
        val letter = TestLetterTemplate(hashMapOf(key to "Some value"), LetterTemplate.EXAMPLE_LETTER)

        // Act
        letterNotificationService.sendLetter(recipientAddress, letter)

        // Assert
        Mockito.verify(notifyClient).sendLetter(any(), any(), any())
    }

    @Test
    fun `Warns that a letter may have been sent when the Notify response cannot be read`() {
        // Arrange
        Mockito
            .doThrow(NotificationClientException("Connection reset"))
            .whenever(notifyClient)
            .sendLetter(any(), any(), any())

        // Act
        val thrownException =
            assertThrows<PersistentLetterSendException> {
                letterNotificationService.sendLetter(recipientAddress, TestLetterTemplate())
            }

        // Assert
        assertTrue(thrownException.message!!.contains("unknown whether that letter was sent"))
    }

    @ParameterizedTest
    @MethodSource("getIndefiniteProblemExceptionMessages")
    fun `Throws PersistentLetterSendException when the service is unable to send letters`(errorMessage: String) {
        // Arrange
        val innerException = NotificationClientException(errorMessage)
        Mockito
            .doThrow(innerException)
            .whenever(notifyClient)
            .sendLetter(any(), any(), any())

        // Act & Assert
        val thrownException =
            assertThrows<PersistentLetterSendException> {
                letterNotificationService.sendLetter(recipientAddress, TestLetterTemplate())
            }
        assertEquals(innerException, thrownException.cause)
    }

    @Test
    fun `Reports a ValidationError as a request rejected by Notify as invalid`() {
        // Arrange
        val errorMessage =
            "Status code: 400 {" +
                "\"errors\":[{\"error\":\"ValidationError\",\"message\":\"Must be a real UK postcode\"}]," +
                "\"status_code\":400}"
        Mockito
            .doThrow(NotificationClientException(errorMessage))
            .whenever(notifyClient)
            .sendLetter(any(), any(), any())

        // Act
        val thrownException =
            assertThrows<PersistentLetterSendException> {
                letterNotificationService.sendLetter(recipientAddress, TestLetterTemplate())
            }

        // Assert
        assertTrue(thrownException.message!!.contains("rejected by Notify as invalid"))
    }

    @Test
    fun `Reports a readable but unrecognised error type as unclassified`() {
        // Arrange
        val errorMessage =
            "Status code: 400 {" +
                "\"errors\":[{\"error\":\"UnrecognisedError\",\"message\":\"Something new\"}]," +
                "\"status_code\":400}"
        Mockito
            .doThrow(NotificationClientException(errorMessage))
            .whenever(notifyClient)
            .sendLetter(any(), any(), any())

        // Act
        val thrownException =
            assertThrows<PersistentLetterSendException> {
                letterNotificationService.sendLetter(recipientAddress, TestLetterTemplate())
            }

        // Assert
        assertTrue(thrownException.message!!.contains("could not be classified"))
    }

    @ParameterizedTest
    @MethodSource("getTemporaryProblemExceptionMessages")
    fun `Throws TransientLetterSendException when the service failed to send that letter`(errorMessage: String) {
        // Arrange
        val innerException = NotificationClientException(errorMessage)
        Mockito
            .doThrow(innerException)
            .whenever(notifyClient)
            .sendLetter(any(), any(), any())

        // Act & Assert
        val thrownException =
            assertThrows<TransientLetterSendException> {
                letterNotificationService.sendLetter(recipientAddress, TestLetterTemplate())
            }
        assertEquals(innerException, thrownException.cause)
    }

    companion object {
        // Error types and messages are taken from Notify's documented errors:
        // https://docs.notifications.service.gov.uk/java.html#send-a-letter-error-codes
        // https://docs.notifications.service.gov.uk/java.html#general-errors
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
                        "\"errors\":[{\"error\":\"BadRequestError\",\"message\":\"Service is not allowed to send letters\"}]," +
                        "\"status_code\":400}",
                ),
                named(
                    "Too Many Requests Error",
                    "Status code: 429 {" +
                        "\"errors\":[{\"error\":\"TooManyRequestsError\",\"message\":\"Exceeded send limits (letter: 50) for today\"}]," +
                        "\"status_code\":429}",
                ),
                named(
                    "Persistent and transient errors",
                    "Status code: 400 {" +
                        "\"errors\":[" +
                        "{\"error\":\"RateLimitError\",\"message\":\"Exceeded rate limit for key type\"}," +
                        "{\"error\":\"BadRequestError\",\"message\":\"Service is not allowed to send letters\"}]," +
                        "\"status_code\":400}",
                ),
                named(
                    "Validation Error",
                    "Status code: 400 {" +
                        "\"errors\":[{\"error\":\"ValidationError\",\"message\":\"Must be a real UK postcode\"}]," +
                        "\"status_code\":400}",
                ),
                named("No response from Notify", "Connection reset"),
            )
    }
}
