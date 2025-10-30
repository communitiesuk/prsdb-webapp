package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.ClassOrderer
import org.junit.jupiter.api.ClassOrdererContext
import org.junit.jupiter.api.TestClassOrder
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.whenever
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
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OsDownloadsConfig
import uk.gov.communities.prsdb.webapp.integration.pageObjects.Navigator
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.service.notify.NotificationClient
import kotlin.reflect.full.isSubclassOf

@Import(TestcontainersConfiguration::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.flyway.clean-disabled=false"],
)
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

    @MockitoBean
    lateinit var absoluteUrlProvider: AbsoluteUrlProvider

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
        val originalRegistration = clientRegistrationRepository.findByRegistrationId("one-login")

        if (originalRegistration != null) {
            val updatedRegistration =
                ClientRegistration
                    // Copy across most properties
                    .withRegistrationId(originalRegistration.registrationId)
                    .clientId(originalRegistration.clientId)
                    .clientSecret(originalRegistration.clientSecret)
                    .clientAuthenticationMethod(originalRegistration.clientAuthenticationMethod)
                    .authorizationGrantType(originalRegistration.authorizationGrantType)
                    .scope(originalRegistration.scopes)
                    .userNameAttributeName(originalRegistration.providerDetails.userInfoEndpoint.userNameAttributeName)
                    // Tweak the URL properties to use the dynamic port
                    .redirectUri("http://localhost:$port/login/oauth2/code/one-login")
                    .authorizationUri("http://localhost:$port/local/one-login/authorize")
                    .tokenUri("http://localhost:$port/local/one-login/token")
                    .jwkSetUri("http://localhost:$port/local/one-login/.well-known/jwks.json")
                    .userInfoUri("http://localhost:$port/local/one-login/userinfo")
                    .build()

            whenever(clientRegistrationRepository.findByRegistrationId("one-login")).thenReturn(updatedRegistration)
        }
    }

    lateinit var navigator: Navigator

    @BeforeEach
    fun setUp(page: Page) {
        navigator = Navigator(page, port)
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
}
