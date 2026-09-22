package uk.gov.communities.prsdb.webapp.integration.oneLoginSimulator

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.mockito.kotlin.whenever
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.communities.prsdb.webapp.integration.IntegrationTest

abstract class OneLoginSimulatorIntegrationTestBase : IntegrationTest() {
    companion object {
        private val clientKeys = OneLoginSimulatorClientKeys.create()
        val simulator = OneLoginSimulatorContainer().apply { start() }

        @JvmStatic
        @DynamicPropertySource
        fun configureOneLoginProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.security.oauth2.client.registration.one-login.client-id") { simulator.clientId }
            registry.add("spring.security.oauth2.client.provider.one-login.authorization-uri") { "${simulator.baseUrl}/authorize" }
            registry.add("spring.security.oauth2.client.provider.one-login.token-uri") { "${simulator.baseUrl}/token" }
            registry.add("spring.security.oauth2.client.provider.one-login.jwk-set-uri") { "${simulator.baseUrl}/.well-known/jwks.json" }
            registry.add("spring.security.oauth2.client.provider.one-login.user-info-uri") { "${simulator.baseUrl}/userinfo" }
            registry.add("one-login.did.uri") { "${simulator.baseUrl}/.well-known/did.json" }
            registry.add("one-login.jwt.public.key") { clientKeys.publicKeyLocation }
            registry.add("one-login.jwt.private.key") { clientKeys.privateKeyLocation }
        }

        @JvmStatic
        @AfterAll
        fun closeSimulator() {
            simulator.close()
            clientKeys.close()
        }
    }

    @BeforeEach
    fun configureClientRegistrationToUseSimulator() {
        simulator.configure(
            clientId = simulator.clientId,
            publicKey = clientKeys.publicKeyPem,
            redirectUrl = "http://localhost:$port/login/oauth2/code/one-login",
            postLogoutRedirectUrl = "http://localhost:$port/signout",
        )

        val localRegistration = clientRegistrationRepository.findByRegistrationId("one-login") ?: return
        val simulatorRegistration =
            ClientRegistration
                .withRegistrationId(localRegistration.registrationId)
                .clientId(simulator.clientId)
                .clientSecret(localRegistration.clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.PRIVATE_KEY_JWT)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .scope(localRegistration.scopes)
                .userNameAttributeName(localRegistration.providerDetails.userInfoEndpoint.userNameAttributeName)
                .redirectUri("http://localhost:$port/login/oauth2/code/one-login")
                .authorizationUri("${simulator.baseUrl}/authorize")
                .tokenUri("${simulator.baseUrl}/token")
                .jwkSetUri("${simulator.baseUrl}/.well-known/jwks.json")
                .userInfoUri("${simulator.baseUrl}/userinfo")
                .build()

        whenever(clientRegistrationRepository.findByRegistrationId("one-login")).thenReturn(simulatorRegistration)
    }
}
