package uk.gov.communities.prsdb.webapp.local.api.controllers

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import java.net.URI
import java.time.Instant
import java.util.Date
import java.util.UUID

@Profile("local")
@PrsdbRestController
@RequestMapping("/local/internal-access")
class MockInternalAccessController(
    @Value("\${server.port}") private val serverPort: String,
) {
    companion object {
        val keyId: String = UUID.randomUUID().toString()

        val rsaKey: RSAKey =
            RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(keyId)
                .algorithm(JWSAlgorithm.RS256)
                .generate()
    }

    private val userId = "ia-mock-user-12345"
    private val userEmail = "test@example.com"
    private val userName = "Mock User"
    private val clientId = "902e8a06-4086-423d-8e67-3838c4e52d7f"

    @Value("\${local.base-path-component}")
    lateinit var basePathComponent: String

    var lastReceivedNonce: String? = null

    @GetMapping("/.well-known/openid-configuration")
    fun openidConfiguration(): ResponseEntity<String> {
        val baseUrl = "http://localhost:$serverPort/local/internal-access"
        val config =
            """
            {
                "issuer": "$baseUrl",
                "authorization_endpoint": "$baseUrl/authorize",
                "token_endpoint": "$baseUrl/token",
                "userinfo_endpoint": "$baseUrl/userinfo",
                "jwks_uri": "$baseUrl/.well-known/jwks.json",
                "end_session_endpoint": "$baseUrl/sign-out",
                "scopes_supported": ["openid", "email", "profile"],
                "response_types_supported": ["code"],
                "grant_types_supported": ["authorization_code"],
                "subject_types_supported": ["public"],
                "id_token_signing_alg_values_supported": ["RS256"],
                "token_endpoint_auth_methods_supported": ["client_secret_basic", "client_secret_post"]
            }
            """.trimIndent()
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(config)
    }

    @GetMapping("/.well-known/jwks.json")
    fun jwksJson(): ResponseEntity<String> {
        val publicJwk = rsaKey.toPublicJWK().toJSONString()
        val jwks = """{"keys": [$publicJwk]}"""
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(jwks)
    }

    @GetMapping("/authorize")
    fun authorize(
        @RequestParam response_type: String,
        @RequestParam client_id: String,
        @RequestParam scope: String,
        @RequestParam state: String,
        @RequestParam redirect_uri: String,
        @RequestParam nonce: String,
    ): ResponseEntity<Unit> {
        lastReceivedNonce = nonce

        val locationURI: URI =
            UriComponentsBuilder
                .newInstance()
                .uri(updateRedirectUri(redirect_uri))
                .query("code=MockInternalAccessCode")
                .queryParam("state", state)
                .build()
                .toUri()

        return ResponseEntity.status(302).location(locationURI).build()
    }

    private fun updateRedirectUri(redirect_uri: String): URI {
        val originalRedirectUri = URI.create(redirect_uri)

        return UriComponentsBuilder
            .fromUri(originalRedirectUri)
            .replacePath(basePathComponent + originalRedirectUri.path)
            .build()
            .toUri()
    }

    @PostMapping("/token")
    fun token(
        @RequestParam grant_type: String,
        @RequestParam redirect_uri: String,
        @RequestParam code: String,
    ): ResponseEntity<String> {
        val idToken = getIdToken()
        val responseBody =
            """
            {
                "access_token": "internal-access-mock-token",
                "token_type": "Bearer",
                "expires_in": 3600,
                "id_token": "$idToken"
            }
            """.trimIndent()

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)
    }

    @GetMapping("/userinfo")
    fun userInfo(): ResponseEntity<String> {
        val responseBody =
            """
            {
                "sub": "$userId",
                "email": "$userEmail",
                "name": "$userName"
            }
            """.trimIndent()

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)
    }

    @GetMapping("/sign-out")
    fun signOut(
        @RequestParam(required = false) post_logout_redirect_uri: String?,
    ): ResponseEntity<Unit> {
        val redirectUri = post_logout_redirect_uri ?: "http://localhost:$serverPort/"
        return ResponseEntity.status(302).location(URI.create(redirectUri)).build()
    }

    private fun getIdToken(): String {
        val header =
            JWSHeader
                .Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(keyId)
                .build()

        val claimsSet =
            JWTClaimsSet
                .Builder()
                .subject(userId)
                .audience(clientId)
                .issuer("http://localhost:$serverPort/local/internal-access")
                .issueTime(Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("nonce", lastReceivedNonce)
                .claim("email", userEmail)
                .claim("name", userName)
                .build()

        val signedJwt = SignedJWT(header, claimsSet)
        signedJwt.sign(RSASSASigner(rsaKey))

        return signedJwt.serialize()
    }
}
