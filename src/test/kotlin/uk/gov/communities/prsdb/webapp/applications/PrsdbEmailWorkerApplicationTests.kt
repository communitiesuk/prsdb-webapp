package uk.gov.communities.prsdb.webapp.applications

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.timeout
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskConfiguration
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ASYNCHRONOUS_EMAIL_SENDING
import uk.gov.communities.prsdb.webapp.local.services.EmailDeliveryStubService
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.BetaFeedbackEmail
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService

// A dedicated queue keeps this test independent of emails other tests leave on the shared email queue
@Import(TestcontainersConfiguration::class, PrsdbEmailWorkerApplicationTests.EmailWorkerTestsQueueConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "email-worker", "local")
@TestPropertySource(
    properties = ["EMAILNOTIFICATIONS_APIKEY", "email-queue.name=$EMAIL_WORKER_TESTS_QUEUE", "email-queue.listener.auto-startup=true"],
)
class PrsdbEmailWorkerApplicationTests {
    @MockitoSpyBean
    lateinit var emailDeliveryStubService: EmailDeliveryStubService

    @Autowired
    lateinit var featureFlagManager: FeatureFlagManager

    @Autowired
    lateinit var emailNotificationService: EmailNotificationService<BetaFeedbackEmail>

    @Autowired
    lateinit var context: ApplicationContext

    @Test
    fun `an email sent with asynchronous sending enabled is delivered by the email queue listener`() {
        featureFlagManager.enableFeature(ASYNCHRONOUS_EMAIL_SENDING)
        val email = BetaFeedbackEmail("feedback", "sender@example.com", "/referrer")

        emailNotificationService.sendEmail("round-trip@example.com", email)

        verify(emailDeliveryStubService, timeout(10_000)).deliverEmail(
            eq("round-trip@example.com"),
            argThat { template == email.template && toHashMap() == email.toHashMap() },
        )
    }

    @Test
    fun `task only PRSDB beans aren't available in email worker mode`() {
        val taskOnlyBeanNames =
            context.getBeanNamesForAnnotation(PrsdbTaskService::class.java) +
                context.getBeanNamesForAnnotation(PrsdbTaskConfiguration::class.java)

        assertEquals(emptyList<String>(), taskOnlyBeanNames.toList())
    }

    // The listener fails to start if its queue doesn't exist, so the queue is created while the context loads
    class EmailWorkerTestsQueueConfiguration {
        @Bean
        fun emailWorkerTestsQueue(sqsAsyncClient: SqsAsyncClient): CreateQueueResponse =
            sqsAsyncClient.createQueue { it.queueName(EMAIL_WORKER_TESTS_QUEUE) }.join()
    }
}

private const val EMAIL_WORKER_TESTS_QUEUE = "email-worker-tests"
