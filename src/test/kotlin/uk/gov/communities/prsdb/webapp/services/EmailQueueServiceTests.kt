package uk.gov.communities.prsdb.webapp.services

import io.awspring.cloud.sqs.operations.SqsTemplate
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.support.TransactionSynchronizationManager
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.QueuedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail

class EmailQueueServiceTests {
    private val sqsTemplate: SqsTemplate = mock()
    private val emailQueueService = EmailQueueService(sqsTemplate, "test-email-queue")
    private val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")
    private val expectedPayload = Json.encodeToString(QueuedEmail.from("recipient@example.com", email))

    @AfterEach
    fun tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization()
        }
    }

    @Test
    fun `enqueue sends the serialised email to the email queue`() {
        emailQueueService.enqueue("recipient@example.com", email)

        verify(sqsTemplate).send("test-email-queue", expectedPayload)
    }

    @Test
    fun `enqueue sends immediately even when transaction synchronization is active`() {
        TransactionSynchronizationManager.initSynchronization()

        emailQueueService.enqueue("recipient@example.com", email)

        verify(sqsTemplate).send("test-email-queue", expectedPayload)
    }

    @Test
    fun `enqueue throws a TransientEmailSentException when sending fails`() {
        whenever(sqsTemplate.send(any<String>(), any<String>())).thenThrow(RuntimeException("SQS unavailable"))

        assertThrows<TransientEmailSentException> { emailQueueService.enqueue("recipient@example.com", email) }
    }
}
