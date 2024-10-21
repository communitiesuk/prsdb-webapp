package uk.gov.communities.prsdb.webapp.integration

import com.microsoft.playwright.junit.UsePlaywright
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.context.ActiveProfiles
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OSPlacesConfig
import uk.gov.communities.prsdb.webapp.config.OneLoginConfig
import uk.gov.service.notify.NotificationClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@UsePlaywright
@ActiveProfiles("integration-test")
abstract class IntegrationTest {
    @LocalServerPort
    val port: Int = 0

    @MockBean
    lateinit var mockClientRegistrationRepository: ClientRegistrationRepository

    @MockBean
    lateinit var mockDefaultAuthorizationCodeTokenResponseClient: DefaultAuthorizationCodeTokenResponseClient

    @MockBean
    lateinit var jwtDecoderFactory: JwtDecoderFactory<ClientRegistration?>

    @MockBean
    lateinit var oneLoginConfig: OneLoginConfig

    @MockBean
    lateinit var notifyConfig: NotifyConfig

    @MockBean
    lateinit var notificationClient: NotificationClient

    @MockBean
    lateinit var OSPlaces: OSPlacesConfig

    @MockBean
    lateinit var OSPlacesClient: OSPlacesClient

    @MockBean
    lateinit var securityFilterChain: SecurityFilterChain
}
