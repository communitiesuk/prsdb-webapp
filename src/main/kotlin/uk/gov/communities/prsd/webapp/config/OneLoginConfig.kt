package uk.gov.communities.prsd.webapp.config

import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.RSAKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.security.web.SecurityFilterChain
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.UUID

@Configuration
class OneLoginConfig {
    @Value("\${one-login.jwt.public.key}")
    lateinit var publicKey: RSAPublicKey

    @Value("\${one-login.jwt.private.key}")
    lateinit var privateKey: RSAPrivateKey

    fun oneloginJWKResolver(clientRegistration: ClientRegistration): JWK? {
        if (clientRegistration.clientAuthenticationMethod.equals(ClientAuthenticationMethod.PRIVATE_KEY_JWT)) {
            val key =
                RSAKey
                    .Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID(UUID.randomUUID().toString())
                    .build()
            return key
        }
        return null
    }

    @Bean
    fun oneloginAuthorizationCodeTokenResponseClient(): DefaultAuthorizationCodeTokenResponseClient {
        val requestEntityConverter = OAuth2AuthorizationCodeGrantRequestEntityConverter()
        requestEntityConverter.addParametersConverter(
            NimbusJwtClientAuthenticationParametersConverter(::oneloginJWKResolver),
        )

        val tokenResponseClient = DefaultAuthorizationCodeTokenResponseClient()
        tokenResponseClient.setRequestEntityConverter(requestEntityConverter)

        return tokenResponseClient
    }

    @Bean
    fun idTokenDecoderFactory(): JwtDecoderFactory<ClientRegistration?> {
        val idTokenDecoderFactory = OidcIdTokenDecoderFactory()
        idTokenDecoderFactory.setJwsAlgorithmResolver { SignatureAlgorithm.ES256 }
        return idTokenDecoderFactory
    }

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .authorizeHttpRequests { requests ->
                requests
                    .requestMatchers("/")
                    .permitAll()
                    .requestMatchers("/assets/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }.oauth2Login(Customizer.withDefaults())
        return http.build()
    }
}
