package uk.gov.communities.prsdb.webapp.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.JWK
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.endpoint.NimbusJwtClientAuthenticationParametersConverter
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwtDecoderFactory
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.web.client.RestClient
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.util.UUID

@Configuration
class OneLoginConfig {
    @Value("\${one-login.jwt.public.key}")
    lateinit var publicKey: RSAPublicKey

    @Value("\${one-login.jwt.private.key}")
    lateinit var privateKey: RSAPrivateKey

    @Value("\${one-login.did.uri}")
    lateinit var didUri: String

    @Bean
    fun oneLoginIdTokenDecoderFactory(): JwtDecoderFactory<Unit> =
        JwtDecoderFactory {
            val publicKeys = retrieveECKeys()
            val processor = DefaultJWTProcessor<SecurityContext>()
            val keySelector = JWSVerificationKeySelector(JWSAlgorithm.ES256, ImmutableJWKSet(JWKSet(publicKeys)))
            processor.jwsKeySelector = keySelector
            NimbusJwtDecoder(processor)
        }

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
    fun oneloginAuthorizationCodeTokenResponseClient() =
        RestClientAuthorizationCodeTokenResponseClient().apply {
            addParametersConverter(NimbusJwtClientAuthenticationParametersConverter(::oneloginJWKResolver))
        }

    @Bean
    fun idTokenDecoderFactory(): JwtDecoderFactory<ClientRegistration?> {
        val idTokenDecoderFactory = OidcIdTokenDecoderFactory()
        idTokenDecoderFactory.setJwsAlgorithmResolver { SignatureAlgorithm.ES256 }
        return idTokenDecoderFactory
    }

    private fun retrieveECKeys(): List<ECKey> {
        val didJson = retrieveDidJsonObject()
        return extractKeysFromJson(didJson)
    }

    private fun extractKeysFromJson(didJson: JSONObject): List<ECKey> {
        val keyArray = didJson.getJSONArray("assertionMethod")

        val publicKeysSequence =
            keyArray
                .map {
                    val keyWrapper = it as JSONObject
                    val jsonKey = keyWrapper.getJSONObject("publicKeyJwk")
                    val keyId = keyWrapper.getString("id")
                    jsonKey.put("kid", keyId)
                    ECKey.parse(jsonKey.toString())
                }
        return publicKeysSequence.toList()
    }

    private fun retrieveDidJsonObject(): JSONObject {
        val didResponseBody =
            RestClient
                .create()
                .get()
                .uri(didUri)
                .retrieve()
                .body(String::class.java)

        return JSONObject(didResponseBody)
    }
}
