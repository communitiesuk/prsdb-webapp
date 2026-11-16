package uk.gov.communities.prsdb.webapp.services

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.NotifyAllowlistException
import uk.gov.communities.prsdb.webapp.exceptions.PersistentEmailSendException
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.QueuedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.EmailTemplateModel

class EmailQueueListenerTests {
    private val emailDeliveryService: EmailDeliveryService = mock()
    private val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")
    private val payload = Json.encodeToString(QueuedEmail.from("recipient@example.com", email))

    private fun listener(fallbackAddress: String = "fallback@example.com") = EmailQueueListener(emailDeliveryService, fallbackAddress)

    private fun hasSameContentAs(expected: EmailTemplateModel) =
        argThat<EmailTemplateModel> { template == expected.template && toHashMap() == expected.toHashMap() }

    @Test
    fun `receiveEmail delivers the queued email to its recipient`() {
        listener().receiveEmail(payload)

        verify(emailDeliveryService).deliverEmail(eq("recipient@example.com"), hasSameContentAs(email))
    }

    @Test
    fun `receiveEmail re-sends to the fallback address when the recipient is not on the Notify allowlist`() {
        whenever(emailDeliveryService.deliverEmail(eq("recipient@example.com"), any()))
            .thenThrow(NotifyAllowlistException("not allowlisted"))

        listener().receiveEmail(payload)

        verify(emailDeliveryService).deliverEmail(eq("fallback@example.com"), hasSameContentAs(email))
    }

    @Test
    fun `receiveEmail acknowledges an allowlist rejection when no fallback address is configured`() {
        whenever(emailDeliveryService.deliverEmail(any(), any()))
            .thenThrow(NotifyAllowlistException("not allowlisted"))

        assertDoesNotThrow { listener(fallbackAddress = "").receiveEmail(payload) }
        verify(emailDeliveryService, times(1)).deliverEmail(any(), any())
    }

    @Test
    fun `receiveEmail rethrows when the fallback send fails so the message is retried`() {
        whenever(emailDeliveryService.deliverEmail(eq("recipient@example.com"), any()))
            .thenThrow(NotifyAllowlistException("not allowlisted"))
        whenever(emailDeliveryService.deliverEmail(eq("fallback@example.com"), any()))
            .thenThrow(TransientEmailSentException("rate limited"))

        assertThrows<TransientEmailSentException> { listener().receiveEmail(payload) }
    }

    @ParameterizedTest
    @MethodSource("deliveryFailures")
    fun `receiveEmail rethrows other delivery failures so the message is retried`(failure: RuntimeException) {
        whenever(emailDeliveryService.deliverEmail(any(), any())).thenThrow(failure)

        val thrown = assertThrows<RuntimeException> { listener().receiveEmail(payload) }

        assertEquals(failure, thrown)
    }

    companion object {
        @JvmStatic
        fun deliveryFailures() =
            listOf(
                TransientEmailSentException("rate limited"),
                PersistentEmailSendException("bad request"),
            )
    }
}
