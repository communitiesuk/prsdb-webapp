package uk.gov.communities.prsdb.webapp.local.services

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import uk.gov.communities.prsdb.webapp.exceptions.NotificationSendFailureReason
import uk.gov.communities.prsdb.webapp.exceptions.SmsSendException
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplate
import uk.gov.communities.prsdb.webapp.models.viewModels.smsModels.SmsTemplateModel
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(OutputCaptureExtension::class)
class SmsNotificationStubServiceTests {
    @Test
    fun `sendSms prints the recipient template and personalisation`(output: CapturedOutput) {
        // Arrange
        val template = mock<SmsTemplate>()
        whenever(template.name).thenReturn("TEST_SMS")
        val sms = mock<SmsTemplateModel>()
        whenever(sms.template).thenReturn(template)
        whenever(sms.toHashMap()).thenReturn(hashMapOf("first name" to "Alex"))

        // Act
        SmsNotificationStubService<SmsTemplateModel>().sendSms("+447700900123", sms)

        // Assert
        assertTrue(output.toString().contains("STUB SMS SENT"))
        assertTrue(output.toString().contains("+447700900123"))
        assertTrue(output.toString().contains("TEST_SMS"))
        assertTrue(output.toString().contains("first name=Alex"))
    }

    @Test
    fun `sendSms simulates technical failures for the configured number`() {
        // Arrange
        val service = SmsNotificationStubService<SmsTemplateModel>()
        service.persistentExceptionPhoneNumber = "+447700900123"

        // Act & Assert
        val exception = assertThrows<SmsSendException> { service.sendSms("+447700900123", mock()) }
        assertEquals(NotificationSendFailureReason.TECHNICAL, exception.reason)
    }

    @Test
    fun `sendSms simulates rate limiting for the configured number`() {
        // Arrange
        val service = SmsNotificationStubService<SmsTemplateModel>()
        service.transientExceptionPhoneNumber = "+447700900123"

        // Act & Assert
        val exception = assertThrows<SmsSendException> { service.sendSms("+447700900123", mock()) }
        assertEquals(NotificationSendFailureReason.MINUTE_LIMIT, exception.reason)
    }
}
