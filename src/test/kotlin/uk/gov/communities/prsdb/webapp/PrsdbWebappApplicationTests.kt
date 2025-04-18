package uk.gov.communities.prsdb.webapp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OSPlacesConfig
import uk.gov.communities.prsdb.webapp.config.OneLoginConfig
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.service.notify.NotificationClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class PrsdbWebappApplicationTests {
    @Test
    fun contextLoads() {
    }

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
    lateinit var osPlacesConfig: OSPlacesConfig

    @MockBean
    lateinit var osPlacesClient: OSPlacesClient

    @MockBean
    lateinit var identityService: OneLoginIdentityService

    @MockBean
    lateinit var s3: S3TransferManager
}
