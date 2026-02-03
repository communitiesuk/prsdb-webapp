package uk.gov.communities.prsdb.webapp.config.featureFlags

import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.config.EpcRegisterConfig
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OneLoginConfig
import uk.gov.communities.prsdb.webapp.config.OsDownloadsConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagTestHelper
import uk.gov.service.notify.NotificationClient

@SpringBootTest(classes = [FeatureFlagConfig::class])
class FeatureFlagTest {
    @Autowired
    private var context: ConfigurableApplicationContext? = null

    @MockitoBean
    lateinit var mockClientRegistrationRepository: ClientRegistrationRepository

    @MockitoBean
    lateinit var mockDefaultAuthorizationCodeTokenResponseClient: DefaultAuthorizationCodeTokenResponseClient

    @MockitoBean
    lateinit var jwtDecoderFactory: JwtDecoderFactory<ClientRegistration?>

    @MockitoBean
    lateinit var oneLoginConfig: OneLoginConfig

    @MockitoBean
    lateinit var notifyConfig: NotifyConfig

    @MockitoBean
    lateinit var notificationClient: NotificationClient

    @MockitoBean
    lateinit var identityService: OneLoginIdentityService

    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3Client

    @MockitoBean
    lateinit var epcConfig: EpcRegisterConfig

    @MockitoBean
    lateinit var epcClient: EpcRegisterClient

    @MockitoBean
    lateinit var epcCertificateUrlProvider: EpcCertificateUrlProvider

    // TODO 1021: Remove OS Download beans when ExampleOsDownloadsController is removed
    @MockitoBean
    lateinit var osDownloadsConfig: OsDownloadsConfig

    @MockitoBean
    lateinit var osDownloadsClient: OsDownloadsClient

    @Autowired
    lateinit var featureFlagManager: FeatureFlagManager

    @Autowired
    lateinit var featureFlagConfig: FeatureFlagConfig

    @AfterEach
    fun resetFeatureFlags() {
        // Reset feature flags to their original configuration from application.yml
        // to prevent test pollution between tests
        FeatureFlagTestHelper.resetToConfiguration(
            featureFlagManager,
            featureFlagConfig.featureFlags,
            featureFlagConfig.releases,
        )
    }
}
