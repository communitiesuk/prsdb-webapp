package uk.gov.communities.prsdb.webapp.applications

import io.awspring.cloud.sqs.operations.SqsTemplate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.timeout
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.CreateQueueResponse
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskConfiguration
import uk.gov.communities.prsdb.webapp.annotations.taskAnnotations.PrsdbTaskService
import uk.gov.communities.prsdb.webapp.local.services.LocalDequarantiningFileCopier
import uk.gov.communities.prsdb.webapp.local.services.LocalQuarantinedFileDeleter
import uk.gov.communities.prsdb.webapp.models.dataModels.GuardDutyScanResultEvent
import uk.gov.communities.prsdb.webapp.models.dataModels.ScanResult
import uk.gov.communities.prsdb.webapp.models.dataModels.UploadedFileLocator
import uk.gov.communities.prsdb.webapp.services.AwsS3DequarantiningFileCopier
import uk.gov.communities.prsdb.webapp.services.AwsS3QuarantinedFileDeleter
import uk.gov.communities.prsdb.webapp.services.LandlordUserEmailService
import uk.gov.communities.prsdb.webapp.services.UploadDequarantiner
import uk.gov.communities.prsdb.webapp.services.VirusNotificationEmailHandler
import uk.gov.communities.prsdb.webapp.services.VirusScanProcessingService
import uk.gov.communities.prsdb.webapp.testHelpers.ApplicationTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.ApplicationTestHelper.Companion.simpleBeanName

// A dedicated queue keeps this test independent of scan results other tests leave on the shared scan result queue
@Import(TestcontainersConfiguration::class, PrsdbScanResultWorkerApplicationTests.ScanResultWorkerTestsQueueConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "email-worker", "scan-result-worker", "local")
@TestPropertySource(
    properties = [
        "EMAILNOTIFICATIONS_APIKEY",
        "scan-result-queue.name=$SCAN_RESULT_WORKER_TESTS_QUEUE",
        "scan-result-queue.listener.auto-startup=true",
    ],
)
class PrsdbScanResultWorkerApplicationTests {
    @MockitoBean
    lateinit var virusScanProcessingService: VirusScanProcessingService

    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3Client

    @Autowired
    lateinit var sqsTemplate: SqsTemplate

    @Autowired
    lateinit var context: ConfigurableApplicationContext

    @Value("\${aws.s3.quarantineBucket}")
    lateinit var quarantineBucketName: String

    @Test
    fun `a scan result sent to the scan result queue is processed by the scan result queue listener`() {
        val locator = UploadedFileLocator("certificateUpload.journey.gas-safety-certificate-upload.1", "e-tag", "version-id")

        sqsTemplate.send(
            SCAN_RESULT_WORKER_TESTS_QUEUE,
            GuardDutyScanResultEvent.of(quarantineBucketName, locator, ScanResult.NoThreats).toJson(),
        )

        verify(virusScanProcessingService, timeout(10_000)).processScan(locator, ScanResult.NoThreats)
    }

    @Test
    fun `scan processing beans are available in scan result worker mode`() {
        val scanProcessingBeanNames =
            listOf(
                UploadDequarantiner::class.simpleBeanName,
                VirusNotificationEmailHandler::class.simpleBeanName,
                LandlordUserEmailService::class.simpleBeanName,
                AwsS3QuarantinedFileDeleter::class.simpleBeanName,
                AwsS3DequarantiningFileCopier::class.simpleBeanName,
                LocalQuarantinedFileDeleter::class.simpleBeanName,
                LocalDequarantiningFileCopier::class.simpleBeanName,
            ).map { it.lowercase() }

        val beanNames = ApplicationTestHelper.getAvailableBeanNames(context)

        assertTrue(beanNames.containsAll(scanProcessingBeanNames)) { "Missing: ${scanProcessingBeanNames - beanNames}" }
    }

    @Test
    fun `task only PRSDB beans aren't available in scan result worker mode`() {
        val taskOnlyBeanNames =
            context.getBeanNamesForAnnotation(PrsdbTaskService::class.java) +
                context.getBeanNamesForAnnotation(PrsdbTaskConfiguration::class.java)

        assertEquals(emptyList<String>(), taskOnlyBeanNames.toList())
    }

    // The listener fails to start if its queue doesn't exist, so the queue is created while the context loads
    class ScanResultWorkerTestsQueueConfiguration {
        @Bean
        fun scanResultWorkerTestsQueue(sqsAsyncClient: SqsAsyncClient): CreateQueueResponse =
            sqsAsyncClient.createQueue { it.queueName(SCAN_RESULT_WORKER_TESTS_QUEUE) }.join()
    }
}

private const val SCAN_RESULT_WORKER_TESTS_QUEUE = "scan-result-worker-tests"
