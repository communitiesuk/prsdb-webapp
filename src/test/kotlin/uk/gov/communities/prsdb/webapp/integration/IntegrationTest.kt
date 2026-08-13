package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.ClassOrdererContext
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.clients.OsDownloadsClient
import uk.gov.communities.prsdb.webapp.config.FeatureFlagConfig
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OsDownloadsConfig
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.integration.pageObjects.Navigator
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.communities.prsdb.webapp.testHelpers.FeatureFlagConfigUpdater
import uk.gov.service.notify.NotificationClient
import java.net.URI
import java.util.function.Predicate
import kotlin.reflect.full.isSubclassOf

@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@UsePlaywright
@ActiveProfiles(profiles = ["local", "local-no-auth"])
@TestClassOrder(IntegrationTest.IntegrationTestOrderer::class)
abstract class IntegrationTest {
    @LocalServerPort
    val port: Int = 0

    @MockitoBean
    lateinit var notifyConfig: NotifyConfig

    @MockitoBean
    lateinit var notificationClient: NotificationClient

    @MockitoBean
    lateinit var identityService: OneLoginIdentityService

    @MockitoSpyBean
    lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3Client

    @MockitoBean
    lateinit var osDownloadsConfig: OsDownloadsConfig

    @MockitoBean
    lateinit var osDownloadsClient: OsDownloadsClient

    @MockitoSpyBean
    lateinit var featureFlagManager: FeatureFlagManager

    @Autowired
    lateinit var featureFlagConfig: FeatureFlagConfig

    /**
     * The mock One Login URLs are hard-coded with port 8080 in the local-no-auth profile config. However, our tests
     * start the application on a random port, so we need to update that config. Unfortunately, the port is not chosen
     * until the server has started, at which point it is too late to use @DynamicPropertySource.
     *
     * Instead, we spy on ClientRegistrationRepository and tweak the various URL values of the ClientRegistration it
     * returns, substituting in the randomly selected port.
     */
    @BeforeEach
    fun setUpClientRegistration() {
        val originalOneLoginRegistration = clientRegistrationRepository.findByRegistrationId("one-login")

        if (originalOneLoginRegistration != null) {
            val updatedRegistration =
                ClientRegistration
                    .withRegistrationId(originalOneLoginRegistration.registrationId)
                    .clientId(originalOneLoginRegistration.clientId)
                    .clientSecret(originalOneLoginRegistration.clientSecret)
                    .clientAuthenticationMethod(originalOneLoginRegistration.clientAuthenticationMethod)
                    .authorizationGrantType(originalOneLoginRegistration.authorizationGrantType)
                    .scope(originalOneLoginRegistration.scopes)
                    .userNameAttributeName(originalOneLoginRegistration.providerDetails.userInfoEndpoint.userNameAttributeName)
                    .redirectUri("http://localhost:$port/login/oauth2/code/one-login")
                    .authorizationUri("http://localhost:$port/local/one-login/authorize")
                    .tokenUri("http://localhost:$port/local/one-login/token")
                    .jwkSetUri("http://localhost:$port/local/one-login/.well-known/jwks.json")
                    .userInfoUri("http://localhost:$port/local/one-login/userinfo")
                    .build()

            whenever(clientRegistrationRepository.findByRegistrationId("one-login")).thenReturn(updatedRegistration)
        }

        val originalInternalAccessRegistration = clientRegistrationRepository.findByRegistrationId("internal-access")

        if (originalInternalAccessRegistration != null) {
            val updatedRegistration =
                ClientRegistration
                    .withRegistrationId(originalInternalAccessRegistration.registrationId)
                    .clientId(originalInternalAccessRegistration.clientId)
                    .clientSecret(originalInternalAccessRegistration.clientSecret)
                    .clientAuthenticationMethod(originalInternalAccessRegistration.clientAuthenticationMethod)
                    .authorizationGrantType(originalInternalAccessRegistration.authorizationGrantType)
                    .scope(originalInternalAccessRegistration.scopes)
                    .userNameAttributeName(originalInternalAccessRegistration.providerDetails.userInfoEndpoint.userNameAttributeName)
                    .redirectUri("http://localhost:$port/local-council/login/oauth2/code/internal-access")
                    .authorizationUri("http://localhost:$port/local/internal-access/authorize")
                    .tokenUri("http://localhost:$port/local/internal-access/token")
                    .jwkSetUri("http://localhost:$port/local/internal-access/.well-known/jwks.json")
                    .userInfoUri("http://localhost:$port/local/internal-access/userinfo")
                    .build()

            whenever(clientRegistrationRepository.findByRegistrationId("internal-access")).thenReturn(updatedRegistration)
        }
    }

    lateinit var navigator: Navigator

    @BeforeEach
    fun setUp(page: Page) {
        navigator = Navigator(page, port)
    }

    /**
     * Every page embeds an analytics script from an external host (see PLAUSIBLE_URL), which is the only
     * external origin the content security policy allows. Playwright waits for the `load` event, and that
     * does not fire until every subresource has settled, so a request that hangs rather than failing fast
     * stalls whichever test is mid-navigation until it times out. Tests only ever talk to the application
     * under test, so everything else is aborted to keep them independent of the network.
     *
     * The route is registered on the browser context so that pages created later, via
     * [createPageAndNavigator], are covered too.
     */
    @BeforeEach
    fun blockRequestsToExternalHosts(browserContext: BrowserContext) {
        browserContext.route(Predicate { url: String -> isExternalUrl(url) }) { route -> route.abort() }
    }

    private fun isExternalUrl(url: String): Boolean {
        // Anything that is not a network request, such as about:blank or a data: URI, has no host to compare
        val host = runCatching { URI(url).host }.getOrNull() ?: return false
        return host !in LOCAL_HOSTS
    }

    @AfterEach
    fun resetFeatureFlags() {
        // Reset feature flags to their original configuration from application.yml
        // to prevent test pollution between integration tests
        FeatureFlagConfigUpdater.resetToConfiguration(
            featureFlagManager,
            featureFlagConfig.featureFlags,
            featureFlagConfig.releases,
        )
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    abstract class NestedIntegrationTest

    class IntegrationTestOrderer : ClassOrderer {
        override fun orderClasses(context: ClassOrdererContext?) {
            // Makes NestedIntegrationTests run last
            context?.classDescriptors?.sortBy { it.testClass.kotlin.isSubclassOf(NestedIntegrationTest::class) }
        }
    }

    fun createPageAndNavigator(browserContext: BrowserContext): Pair<Page, Navigator> {
        val page = browserContext.newPage()
        val navigator = Navigator(page, port)
        return Pair(page, navigator)
    }

    companion object {
        private val LOCAL_HOSTS = setOf("localhost", "127.0.0.1")
    }
}
