package uk.gov.communities.prsdb.webapp.applications

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.PrsdbWebappApplication
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.FeatureFlipStrategyInitialiser
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OsDownloadsConfig
import uk.gov.communities.prsdb.webapp.config.S3Config
import uk.gov.communities.prsdb.webapp.database.repository.LandlordSearchRepositoryImpl
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipSearchRepositoryImpl
import uk.gov.communities.prsdb.webapp.local.services.EmailNotificationStubService
import uk.gov.communities.prsdb.webapp.local.services.LocalDequarantiningFileCopier
import uk.gov.communities.prsdb.webapp.local.services.LocalQuarantinedFileDeleter
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.AwsS3DequarantiningFileCopier
import uk.gov.communities.prsdb.webapp.services.AwsS3QuarantinedFileDeleter
import uk.gov.communities.prsdb.webapp.services.LandlordIncompletePropertiesService
import uk.gov.communities.prsdb.webapp.services.NgdAddressLoader
import uk.gov.communities.prsdb.webapp.services.NotifyEmailNotificationService
import uk.gov.communities.prsdb.webapp.services.NotifyIdService
import uk.gov.communities.prsdb.webapp.services.UploadDequarantiner
import uk.gov.communities.prsdb.webapp.services.VirusAlertSender
import uk.gov.communities.prsdb.webapp.services.VirusScanProcessingService
import uk.gov.communities.prsdb.webapp.testHelpers.ApplicationTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.ApplicationTestHelper.Companion.simpleBeanName

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("web-server-deactivated", "local")
// These environment variables are required for the expected beans to be created - values aren't needed
@TestPropertySource(properties = ["EMAILNOTIFICATIONS_APIKEY", "OS_API_KEY"])
class PrsdbTaskApplicationTests {
    @Autowired
    private var context: ConfigurableApplicationContext? = null

    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3Client

    @MockitoBean
    lateinit var osDownloadsClient: OsDownloadsClient

    @Test
    fun `only necessary PRSDB beans are available in non web mode`() {
        val expectedBeansByName =
            listOf(
                PrsdbWebappApplication::class.simpleBeanName,
                EmailNotificationStubService::class.simpleBeanName,
                NotifyEmailNotificationService::class.simpleBeanName,
                NotifyConfig::class.simpleBeanName,
                S3Config::class.simpleBeanName,
                AwsS3QuarantinedFileDeleter::class.simpleBeanName,
                LocalQuarantinedFileDeleter::class.simpleBeanName,
                AwsS3DequarantiningFileCopier::class.simpleBeanName,
                LocalDequarantiningFileCopier::class.simpleBeanName,
                UploadDequarantiner::class.simpleBeanName,
                VirusScanProcessingService::class.simpleBeanName,
                AbsoluteUrlProvider::class.simpleBeanName,
                VirusAlertSender::class.simpleBeanName,
                OsDownloadsConfig::class.simpleBeanName,
                NotifyIdService::class.simpleBeanName,
                TestcontainersConfiguration::class.simpleBeanName,
                NgdAddressLoader::class.simpleBeanName,
                FeatureFlagConfig::class.simpleBeanName,
                FeatureFlipStrategyInitialiser::class.simpleBeanName,
                PropertyOwnershipSearchRepositoryImpl::class.simpleBeanName,
                LandlordSearchRepositoryImpl::class.simpleBeanName,
                LandlordIncompletePropertiesService::class.simpleBeanName,
            ).map { it.lowercase() }.toSet()

        val beanNames = ApplicationTestHelper.getAvailableBeanNames(context!!)

        assertEquals(expectedBeansByName, beanNames) { buildHelpfulErrorMessage(expectedBeansByName, beanNames) }
    }

    fun buildHelpfulErrorMessage(
        expectedBeans: Set<String>,
        actualBeans: Set<String>,
    ): String {
        val missingBeans = expectedBeans.filter { !actualBeans.contains(it) }
        val unexpectedBeans = actualBeans.filter { !expectedBeans.contains(it) }

        val combinedMessage =
            if (missingBeans.isNotEmpty() && unexpectedBeans.isNotEmpty()) {
                """There are beans missing that are expected and unexpected beans present in non web server mode.
                |This could be because some beans have been renamed, or there may be multiple problems that need addressing:
                """.trimMargin()
            } else {
                null
            }
        val missingBeansMessage =
            if (missingBeans.isNotEmpty()) {
                """The following beans expected for non web server mode are missing - check they haven't been changed to web only or removed (or remove them from the expected list):
                |${missingBeans.joinToString("\n- ", prefix = "- ")}
                """.trimMargin()
            } else {
                null
            }
        val unexpectedBeansMessage =
            if (unexpectedBeans.isNotEmpty()) {
                """The following beans are being created in non web server mode but have not been added to the expected list: 
                |${unexpectedBeans.joinToString("\n- ", prefix = "- ")}
                """.trimMargin()
            } else {
                null
            }

        return listOfNotNull(combinedMessage, missingBeansMessage, unexpectedBeansMessage).joinToString("\n\n")
    }
}
