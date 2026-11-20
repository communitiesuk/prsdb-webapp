package uk.gov.communities.prsdb.webapp.applications

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.transfer.s3.S3TransferManager
import uk.gov.communities.prsdb.webapp.TestcontainersConfiguration
import uk.gov.communities.prsdb.webapp.clients.EpcRegisterClient
import uk.gov.communities.prsdb.webapp.config.EpcRegisterConfig
import uk.gov.communities.prsdb.webapp.config.NotifyConfig
import uk.gov.communities.prsdb.webapp.config.OneLoginConfig
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.services.OneLoginIdentityService
import uk.gov.communities.prsdb.webapp.testHelpers.ApplicationTestHelper
import uk.gov.service.notify.NotificationClient

@Import(TestcontainersConfiguration::class)
@SpringBootTest
@ActiveProfiles("local")
// These environment variables are required for the expected beans to be created - values aren't needed
@TestPropertySource(properties = ["EMAILNOTIFICATIONS_APIKEY", "OS_API_KEY"])
class PrsdbWebappApplicationTests {
    @Autowired
    private var context: ConfigurableApplicationContext? = null

    @MockitoBean
    lateinit var mockClientRegistrationRepository: ClientRegistrationRepository

    @MockitoBean
    lateinit var mockDefaultAuthorizationCodeTokenResponseClient: RestClientAuthorizationCodeTokenResponseClient

    @MockitoBean
    lateinit var jwtDecoderFactory: JwtDecoderFactory<ClientRegistration>

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

    @Test
    fun contextLoads() {
    }

    @Test
    fun `process only PRSDB beans aren't available in web mode`() {
        val processOnlyBeanTypes = ApplicationTestHelper.getProcessOnlyBeanTypes()

        val unexpectedBeanNames =
            processOnlyBeanTypes
                .flatMap { context!!.getBeanNamesForType(it).asIterable() }
                .toSet()

        assertEquals(emptySet<String>(), unexpectedBeanNames) { buildHelpfulErrorMessage(unexpectedBeanNames) }
    }

    fun buildHelpfulErrorMessage(unexpectedBeanNames: Set<String>) =
        """The following process only beans are being created in web server mode:
        | ${unexpectedBeanNames.joinToString("\n- ", prefix = "- ")}
        """.trimMargin()
}
