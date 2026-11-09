package uk.gov.communities.prsdb.webapp.services

import io.awspring.cloud.sqs.operations.SqsTemplate
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.exceptions.TransientEmailSentException
import uk.gov.communities.prsdb.webapp.models.dataModels.QueuedEmail
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail

// A dedicated queue keeps this test independent of email queue listeners running in other cached test contexts
@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "local")
@TestPropertySource(
    properties = ["EMAILNOTIFICATIONS_APIKEY", "OS_API_KEY", "email-queue.name=$EMAIL_QUEUE_SERVICE_SQS_TESTS_QUEUE"],
)
class EmailQueueServiceSqsTests {
    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3Client

    @MockitoBean
    lateinit var osDownloadsClient: OsDownloadsClient

    @Autowired
    lateinit var emailQueueService: EmailQueueService

    @Autowired
    lateinit var sqsTemplate: SqsTemplate

    @Autowired
    lateinit var sqsAsyncClient: SqsAsyncClient

    private val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")

    @BeforeEach
    fun createQueue() {
        sqsAsyncClient.createQueue { it.queueName(EMAIL_QUEUE_SERVICE_SQS_TESTS_QUEUE) }.join()
    }

    @Test
    fun `enqueue puts the serialised email on the email queue`() {
        emailQueueService.enqueue("recipient@example.com", email)

        val message = sqsTemplate.receive(EMAIL_QUEUE_SERVICE_SQS_TESTS_QUEUE, String::class.java)
        assertEquals(
            QueuedEmail.from("recipient@example.com", email),
            Json.decodeFromString<QueuedEmail>(message.get().payload),
        )
    }

    @Test
    fun `enqueue fails without creating the queue when the queue does not exist`() {
        val missingQueueService = EmailQueueService(sqsTemplate, MISSING_QUEUE)

        assertThrows<TransientEmailSentException> { missingQueueService.enqueue("recipient@example.com", email) }

        assertTrue(sqsAsyncClient.listQueues { it.queueNamePrefix(MISSING_QUEUE) }.join().queueUrls().isEmpty())
    }
}

private const val EMAIL_QUEUE_SERVICE_SQS_TESTS_QUEUE = "email-queue-service-sqs-tests"
private const val MISSING_QUEUE = "email-queue-service-sqs-tests-missing-queue"
