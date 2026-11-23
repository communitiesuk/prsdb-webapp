package uk.gov.communities.prsdb.webapp.services

import org.json.JSONException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.SmsSendException
import uk.gov.communities.prsdb.webapp.exceptions.SmsSendFailureReason
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

    @BeforeEach
    fun setUp() {
        whenever(notifyIdService.getNotifyIdValue(any<SmsTemplate>())).thenReturn("template-id")
    }

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

        // Act
        service.sendSms("+447700900123", sms)

        // Assert
        verify(notifyClient).sendSms("template-id", "+447700900123", personalisation, null)
    }

    @Test
    fun `sendSms treats a Notify server error as an uncertain outcome`() {
        val cause =
            NotificationClientException(
                """Status code: 500 {"errors":[{"error":"Exception","message":"Server error"}],"status_code":500}""",
            )
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        val exception = assertThrows<SmsSendException> { service.sendSms("+447700900123", TestSmsTemplate()) }

        assertEquals(cause, exception.cause)
        assertEquals(SmsSendFailureReason.UNKNOWN, exception.reason)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", " "])
    fun `sendSms rejects an unconfigured template without calling Notify`(templateId: String) {
        // Arrange
        val sms = TestSmsTemplate()
        whenever(notifyIdService.getNotifyIdValue(sms.template)).thenReturn(templateId)

        // Act
        val exception = assertThrows<SmsSendException> { service.sendSms("+447700900123", sms) }

        // Assert
        assertTrue(exception.message!!.contains("No Notify template ID"))
        assertEquals(SmsSendFailureReason.TECHNICAL, exception.reason)
        verifyNoInteractions(notifyClient)
    }

    @ParameterizedTest
    @CsvSource(
        "AuthError, TECHNICAL",
        "BadRequestError, INVALID_REQUEST",
        "ValidationError, INVALID_REQUEST",
        "TooManyRequestsError, DAILY_LIMIT",
        "RateLimitError, MINUTE_LIMIT",
        "Exception, UNKNOWN",
    )
    fun `sendSms classifies Notify errors`(
        errorType: String,
        reason: SmsSendFailureReason,
    ) {
        // Arrange
        val cause = NotificationClientException(errorResponse(errorType))
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<SmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertEquals(reason, exception.reason)
    }

    @Test
    fun `sendSms treats a server response as uncertain even if its error type is recognised`() {
        // Arrange
        val cause = NotificationClientException(errorResponse("AuthError").replace("400", "500"))
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<SmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertEquals(SmsSendFailureReason.UNKNOWN, exception.reason)
    }

    @Test
    fun `sendSms prioritises invalid requests over minute limits`() {
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
            assertThrows<SmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertEquals(SmsSendFailureReason.INVALID_REQUEST, exception.reason)
    }

    @ParameterizedTest
    @CsvSource(
        "AuthError, TooManyRequestsError, TECHNICAL",
        "TooManyRequestsError, ValidationError, DAILY_LIMIT",
        "NewError, AuthError, UNKNOWN",
        "Exception, AuthError, UNKNOWN",
    )
    fun `sendSms classifies mixed errors conservatively`(
        first: String,
        second: String,
        reason: SmsSendFailureReason,
    ) {
        val cause =
            NotificationClientException(
                """Status code: 400 {"errors":[""" +
                    """{"error":"$first","message":"Error"},""" +
                    """{"error":"$second","message":"Error"}],"status_code":400}""",
            )
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        val exception = assertThrows<SmsSendException> { service.sendSms("+447700900123", TestSmsTemplate()) }

        assertEquals(reason, exception.reason)
        assertEquals(cause, exception.cause)
    }

    @ParameterizedTest
    @ValueSource(strings = ["Connection reset", """Status code: 400 {"unexpected":"response"}"""])
    fun `sendSms warns about uncertain outcomes when the error response cannot be read`(error: String) {
        // Arrange
        val cause = NotificationClientException(error)
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<SmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertEquals(SmsSendFailureReason.UNKNOWN, exception.reason)
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
            assertThrows<SmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertEquals(SmsSendFailureReason.UNKNOWN, exception.reason)
    }

    @Test
    fun `sendSms preserves an unreadable success response as the cause of an uncertain outcome`() {
        // Arrange
        val cause = JSONException("Invalid response")
        doThrow(cause).whenever(notifyClient).sendSms(anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull())

        // Act
        val exception =
            assertThrows<SmsSendException> {
                service.sendSms("+447700900123", TestSmsTemplate())
            }

        // Assert
        assertEquals(cause, exception.cause)
        assertEquals(SmsSendFailureReason.UNKNOWN, exception.reason)
    }

    private fun errorResponse(errorType: String): String =
        """Status code: 400 {"errors":[{"error":"$errorType","message":"Notify error details"}],"status_code":400}"""
}
