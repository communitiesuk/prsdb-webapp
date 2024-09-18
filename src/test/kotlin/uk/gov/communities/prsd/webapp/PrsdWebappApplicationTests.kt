package uk.gov.communities.prsd.webapp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import uk.gov.communities.prsd.webapp.config.OneLoginConfig

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class PrsdWebappApplicationTests {
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
}
