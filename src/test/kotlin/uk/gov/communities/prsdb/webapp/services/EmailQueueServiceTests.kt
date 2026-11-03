package uk.gov.communities.prsdb.webapp.services

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail

class EmailQueueServiceTests {
    private val sqsTemplate: SqsTemplate = mock()
    private val emailQueueService = EmailQueueService(sqsTemplate, "test-email-queue")
    private val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")

    @Test
    fun `enqueue throws a TransientEmailSentException when sending fails`() {
        whenever(sqsTemplate.send(any<String>(), any<String>())).thenThrow(RuntimeException("SQS unavailable"))

        assertThrows<TransientEmailSentException> { emailQueueService.enqueue("recipient@example.com", email) }
    }
}
