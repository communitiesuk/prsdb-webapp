package uk.gov.communities.prsdb.webapp.services

import org.json.JSONException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.PersistentSmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientSmsSendException
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplateModel
import uk.gov.service.notify.NotificationClient
import uk.gov.service.notify.NotificationClientException
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class NotifySmsNotificationServiceTests {
    @Mock
    private lateinit var notifyClient: NotificationClient

    @Mock
    private lateinit var notifyIdService: NotifyIdService

    @InjectMocks
    private lateinit var service: NotifySmsNotificationService<TestSmsTemplate>

    private class TestSmsTemplate(
        private val personalisation: HashMap<String, String> = hashMapOf(),
        override val template: SmsTemplate = mock(),
    ) : SmsTemplateModel {
        override fun toHashMap(): HashMap<String, String> = personalisation
    }

    @Test
    fun `sendSms sends a matching SMS using the notification client`() {
        // Arrange
        val personalisation = hashMapOf("first name" to "Alex")
        val sms = TestSmsTemplate(personalisation)
        whenever(notifyIdService.getNotifyIdValue(sms.template)).thenReturn("template-id")

        // Act
        service.sendSms("+447700900123", sms)

        // Assert
        verify(notifyClient).sendSms("template-id", "+447700900123", personalisation, null)
    }

    @ParameterizedTest
    @ValueSource(strings = ["AuthError", "BadRequestError", "ValidationError", "TooManyRequestsError"])
    fun `sendSms throws PersistentSmsSendException when the request cannot succeed`(errorType: String) {
        // Arrange
        val cause = NotificationClientException(errorResponse(errorType))
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<PersistentSmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
    }

    @ParameterizedTest
    @ValueSource(strings = ["RateLimitError", "Exception"])
    fun `sendSms throws TransientSmsSendException when retrying may succeed`(errorType: String) {
        // Arrange
        val cause = NotificationClientException(errorResponse(errorType))
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<TransientSmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
    }

    @Test
    fun `sendSms prioritises persistent failures over transient failures`() {
        // Arrange
        val cause =
            NotificationClientException(
                """Status code: 400 {"errors":[""" +
                    """{"error":"RateLimitError","message":"Rate limit"},""" +
                    """{"error":"BadRequestError","message":"Recipient not allowed"}],"status_code":400}""",
            )
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<PersistentSmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertTrue(exception.message!!.startsWith("There were multiple errors"))
    }

    @ParameterizedTest
    @ValueSource(strings = ["Connection reset", """Status code: 400 {"unexpected":"response"}"""])
    fun `sendSms warns about uncertain outcomes when the error response cannot be read`(error: String) {
        // Arrange
        val cause = NotificationClientException(error)
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<PersistentSmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertTrue(exception.message!!.contains("unknown whether that SMS was sent"))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            """Status code: 400 {"errors":[{"error":"NewError","message":"Unknown error"}],"status_code":400}""",
            """Status code: 400 {"errors":[],"status_code":400}""",
        ],
    )
    fun `sendSms reports unclassified errors instead of silently returning`(error: String) {
        // Arrange
        val cause = NotificationClientException(error)
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<PersistentSmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertTrue(exception.message!!.contains("could not be classified"))
    }

    @Test
    fun `sendSms preserves an unreadable success response as the cause of an uncertain outcome`() {
        // Arrange
        val cause = JSONException("Invalid response")
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<PersistentSmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertTrue(exception.message!!.contains("unknown whether that SMS was sent"))
    }

    private fun errorResponse(errorType: String): String =
        """Status code: 400 {"errors":[{"error":"$errorType","message":"Notify error details"}],"status_code":400}"""
}
