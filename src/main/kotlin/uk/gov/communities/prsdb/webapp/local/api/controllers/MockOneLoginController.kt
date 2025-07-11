package uk.gov.communities.prsdb.webapp.local.api.controllers

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWT
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.PrsdbRestController
import java.io.File
import java.net.URI
import java.time.Instant
import java.util.Date
import java.util.UUID

@Profile("local")
@PrsdbRestController
@RequestMapping("/local/one-login")
class MockOneLoginController {
    companion object {
        val keyId = UUID.randomUUID().toString()

        val ecKey = generateSecretKey()

        private fun generateSecretKey(): ECKey {
            val ecKey =
                ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(keyId)
                    .algorithm(Algorithm("ES256"))
                    .generate()

            return ecKey
        }

        val coreIdentityJwt = generateCoreIdentityJwt()

        private fun generateCoreIdentityJwt(): JWT {
            val verifiableCredentialString =
                File(
                    "src/main/kotlin/uk/gov/communities/prsdb/webapp/local/api/mockOneLoginResponses/verifiableCredential.json",
                ).readText(Charsets.UTF_8).trim()
            val verifiableCredential = JSONObject(verifiableCredentialString).toMap()
            val claimsSet = JWTClaimsSet.Builder().claim("vc", verifiableCredential).build()
            val header = JWSHeader.Builder(JWSAlgorithm.ES256).keyID(keyId).build()
            val signedJWT = SignedJWT(header, claimsSet)
            signedJWT.sign(ECDSASigner(ecKey))
            return signedJWT
        }

        var userInfoFile = "src/main/kotlin/uk/gov/communities/prsdb/webapp/local/api/mockOneLoginResponses/userInfo.json"
        const val VERIFIED_USER_FILE =
            "src/main/kotlin/uk/gov/communities/prsdb/webapp/local/api/mockOneLoginResponses/verifiedUserInfo.json"
    }

    @Value("\${local.id-verification-user-info-file:${VERIFIED_USER_FILE}}")
    lateinit var postVerificationUserInfoFile: String

    private val userId = "urn:fdc:gov.uk:2022:UVWXY"

    // These values are from One-Login's publicly available docs (https://docs.sign-in.service.gov.uk/integrate-with-integration-environment/authenticate-your-user/)
    private val userEmail = "test@example.com"
    private val userNumber = "01406946277"

    var lastReceivedNonce: String? = null

    @GetMapping("/.well-known/openid-configuration")
    fun openidConfiguration(): String =
        File("src/main/kotlin/uk/gov/communities/prsdb/webapp/local/api/mockOneLoginResponses/openid-configuration.json")
            .readText(Charsets.UTF_8)

    @GetMapping("/.well-known/jwks.json")
    fun jwksJson(): String =
        File("src/main/kotlin/uk/gov/communities/prsdb/webapp/local/api/mockOneLoginResponses/jwks.json")
            .readText(Charsets.UTF_8)
            .replace("keyId", keyId)
            .replace(
                "publicJWK_x",
                ecKey
                    .toPublicJWK()
                    .x
                    .toString(),
            ).replace(
                "publicJWK_y",
                ecKey
                    .toPublicJWK()
                    .y
                    .toString(),
            )

    @GetMapping("/.well-known/did.json")
    fun didsJson(): String =
        File("src/main/kotlin/uk/gov/communities/prsdb/webapp/local/api/mockOneLoginResponses/did.json")
            .readText(Charsets.UTF_8)
            .replace("keyId", keyId)
            .replace(
                "publicJWK_x",
                ecKey
                    .toPublicJWK()
                    .x
                    .toString(),
            ).replace(
                "publicJWK_y",
                ecKey
                    .toPublicJWK()
                    .y
                    .toString(),
            )

    @GetMapping("/authorize")
    fun authorize(
        @RequestParam response_type: String,
        @RequestParam client_id: String,
        @RequestParam scope: String,
        @RequestParam state: String,
        @RequestParam redirect_uri: String,
        @RequestParam nonce: String,
        @RequestParam vtr: String?,
    ): ResponseEntity<Unit> {
        lastReceivedNonce = nonce

        val locationURI: URI =
            UriComponentsBuilder
                .newInstance()
                .uri(updateRedirectUri(redirect_uri))
                .query("code=SplxlOBeZQQYbYS6WxSbIA")
                .queryParam("state", state)
                .build()
                .toUri()

        if (vtr != null) {
            userInfoFile = postVerificationUserInfoFile
        }

        return ResponseEntity.status(302).location(locationURI).build()
    }

    // Normally locally we want to give ourselves all roles, but to simulate only having access for a specific
    // deployed service change the basePathComponent as required.
    private fun updateRedirectUri(redirect_uri: String): URI {
        val originalRedirectUri = URI.create(redirect_uri)
        val basePathComponent = ""
        // val basePathComponent = "/local-authority"
        // val basePathComponent = "/landlord"

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
        @RequestParam client_assertion: String,
        @RequestParam client_assertion_type: String,
        @RequestParam code: String,
    ): ResponseEntity<String> {
        val responseBody =
            File("src/main/kotlin/uk/gov/communities/prsdb/webapp/local/api/mockOneLoginResponses/token.json")
                .readText(Charsets.UTF_8)
                .replace("idToken", getIdToken())

        val responseBuild = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)

        return responseBuild
    }

    @GetMapping("/userinfo")
    fun userInfo(): ResponseEntity<String> {
        val responseBody =
            File(userInfoFile)
                .readText(Charsets.UTF_8)
                .replace("userId", userId)
                .replace("userEmail", userEmail)
                .replace("userNumber", userNumber)
                .replace("coreIdentityJwt", coreIdentityJwt.serialize())

        val responseBuild = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)

        return responseBuild
    }

    private fun getIdToken(): String {
        val headerBuilder: JWSHeader.Builder =
            JWSHeader
                .Builder(JWSAlgorithm.ES256)
                .type(JOSEObjectType.JWT)
                .jwk(ecKey.toPublicJWK())

        val claimSetBBuilder: JWTClaimsSet.Builder =
            JWTClaimsSet
                .Builder()
                .subject(userId)
                .audience("l0AE7SbEHrEa8QeQCGdml9KQ4bk")
                .issuer("http://localhost:8080/one-login-local/")
                .issueTime(Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("vot", "Cl.Cm")
                .claim("nonce", lastReceivedNonce)
                .claim("vtm", "http://localhost:8080/one-login-local/trustmark")
                .claim("sid", "dX5xv0XgHh6yfD1xy-ss_1EDK0I")

        val signedJwt = SignedJWT(headerBuilder.build(), claimSetBBuilder.build())

        signedJwt.sign(ECDSASigner(ecKey))

        return signedJwt.serialize()
    }
}
