package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.Page
import com.microsoft.playwright.junit.UsePlaywright
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.test.context.ActiveProfiles
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OSPlacesConfig
import uk.gov.communities.prsdb.webapp.integration.pageObjects.Navigator
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.service.notify.NotificationClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.flyway.clean-disabled=false"],
)
@UsePlaywright
@ActiveProfiles(profiles = ["local", "local-no-auth"])
abstract class IntegrationTest {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    lateinit var notifyConfig: NotifyConfig

    @MockBean
    lateinit var notificationClient: NotificationClient

    @MockBean
    lateinit var osPlacesConfig: OSPlacesConfig

    @MockBean
    lateinit var osPlacesClient: OSPlacesClient

    @MockBean
    lateinit var identityService: OneLoginIdentityService

    @MockBean
    lateinit var absoluteUrlProvider: AbsoluteUrlProvider

    @SpyBean
    lateinit var clientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    lateinit var s3: S3TransferManager

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
        navigator = Navigator(page, port, identityService)
    }

    @AfterEach
    fun resetDatabase(
        @Autowired flyway: Flyway,
    ) {
        flyway.clean()
        flyway.migrate()
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun resetDatabaseBeforeAll(
            @Autowired flyway: Flyway,
        ) {
            flyway.clean()
            flyway.migrate()
        }
    }
}
