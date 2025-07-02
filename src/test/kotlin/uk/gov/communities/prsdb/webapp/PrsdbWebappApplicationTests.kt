package uk.gov.communities.prsdb.webapp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.clients.OSPlacesClient
import uk.gov.communities.prsdb.webapp.config.EpcRegisterConfig
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OSPlacesConfig
import uk.gov.communities.prsdb.webapp.config.OneLoginConfig
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.service.notify.NotificationClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class PrsdbWebappApplicationTests {
    @Test
    fun contextLoads() {
    }

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
    lateinit var osPlacesConfig: OSPlacesConfig

    @MockitoBean
    lateinit var osPlacesClient: OSPlacesClient

    @MockitoBean
    lateinit var identityService: OneLoginIdentityService

    @MockitoBean
    lateinit var s3: S3TransferManager

    @MockitoBean
    lateinit var s3client: S3AsyncClient

    @MockitoBean
    lateinit var epcConfig: EpcRegisterConfig

    @MockitoBean
    lateinit var epcClient: EpcRegisterClient

    @MockitoBean
    lateinit var epcCertificateUrlProvider: EpcCertificateUrlProvider
}
