package uk.gov.communities.prsd.webapp

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository

@Import(TestcontainersConfiguration::class)
@SpringBootTest
class PrsdWebappApplicationTests {
    @Test
    fun contextLoads() {
    }

    @MockBean
    lateinit var mockClientRegistrationRepository: ClientRegistrationRepository
}
