package uk.gov.communities.prsdb.webapp.local.api.mockOneLogin

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.jwk.Curve
import com.nimbusds.jose.jwk.ECKey
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.gen.ECKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.util.Date

class MockOneLoginHelper {
    companion object {
        val ecKey = generateSecretKey()

        private fun generateSecretKey(): ECKey {
            val ecKey =
                ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID("6a4bc1e3-9530-4d5b-90c5-10dcf3ffccd0")
                    .algorithm(Algorithm("ES256"))
                    .generate()

            return ecKey
        }
    }

    private val userId = "urn:fdc:gov.uk:2022:PQRST"
    private val userEmail = "julia.jones@hotmail.com"
    private val userNumber = "07123456789"

    val authorizationCode = "SplxlOBeZQQYbYS6WxSbIA"

    var lastReceivedNonce: String? = null

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
                .claim("sid", "Nzk0M2NiNWUtYWZhNC00ZjZmLThiOTItNzUxNjcyNjUwOGNl")

        val signedJwt = SignedJWT(headerBuilder.build(), claimSetBBuilder.build())

        signedJwt.sign(ECDSASigner(ecKey))

        return signedJwt.serialize()
    }

    fun getOpenidConfigurationResponse(): String =
        "{\n" +
            "\"authorization_endpoint\": \"http://localhost:8080/one-login-local/authorize\",\n" +
            "\"token_endpoint\": \"http://localhost:8080/one-login-local/token\",\n" +
            "\"registration_endpoint\": \"http://localhost:8080/one-login-local/connect/register\",\n" +
            "\"issuer\": \"http://localhost:8080/one-login-local/\",\n" +
            "\"jwks_uri\": \"http://localhost:8080/one-login-local/.well-known/jwks.json\",\n" +
            "\"scopes_supported\": [\n" +
            "\"openid\",\n" +
            "\"email\",\n" +
            "\"phone\",\n" +
            "\"offline_access\"\n" +
            "],\n" +
            "\"response_types_supported\": [\n" +
            "\"code\"\n" +
            "],\n" +
            "\"grant_types_supported\": [\n" +
            "\"authorization_code\"\n" +
            "],\n" +
            "\"token_endpoint_auth_methods_supported\": [\n" +
            "\"private_key_jwt\",\n" +
            "\"client_secret_post\"\n" +
            "],\n" +
            "\"token_endpoint_auth_signing_alg_values_supported\": [\n" +
            "\"RS256\",\n" +
            "\"RS384\",\n" +
            "\"RS512\",\n" +
            "\"PS256\",\n" +
            "\"PS384\",\n" +
            "\"PS512\"\n" +
            "],\n" +
            "\"ui_locales_supported\": [\n" +
            "\"en\",\n" +
            "\"cy\"\n" +
            "],\n" +
            "\"service_documentation\": \"https://docs.sign-in.service.gov.uk/\",\n" +
            "\"op_policy_uri\": \"https://signin.integration.account.gov.uk/privacy-notice\",\n" +
            "\"op_tos_uri\": \"https://signin.integration.account.gov.uk/terms-and-conditions\",\n" +
            "\"request_parameter_supported\": true,\n" +
            "\"trustmarks\": \"http://localhost:8080/one-login-local/trustmark\",\n" +
            "\"subject_types_supported\": [\n" +
            "\"public\",\n" +
            "\"pairwise\"\n" +
            "],\n" +
            "\"userinfo_endpoint\": \"http://localhost:8080/one-login-local/userinfo\",\n" +
            "\"end_session_endpoint\": \"http://localhost:8080/one-login-local/logout\",\n" +
            "\"id_token_signing_alg_values_supported\": [\n" +
            "\"ES256\",\n" +
            "\"RS256\"\n" +
            "],\n" +
            "\"claim_types_supported\": [\n" +
            "\"normal\"\n" +
            "],\n" +
            "\"claims_supported\": [\n" +
            "\"sub\",\n" +
            "\"email\",\n" +
            "\"email_verified\",\n" +
            "\"phone_number\",\n" +
            "\"phone_number_verified\",\n" +
            "\"wallet_subject_id\",\n" +
            "\"https://vocab.account.gov.uk/v1/passport\",\n" +
            "\"https://vocab.account.gov.uk/v1/socialSecurityRecord\",\n" +
            "\"https://vocab.account.gov.uk/v1/drivingPermit\",\n" +
            "\"https://vocab.account.gov.uk/v1/coreIdentityJWT\",\n" +
            "\"https://vocab.account.gov.uk/v1/address\",\n" +
            "\"https://vocab.account.gov.uk/v1/inheritedIdentityJWT\",\n" +
            "\"https://vocab.account.gov.uk/v1/returnCode\"\n" +
            "],\n" +
            "\"request_uri_parameter_supported\": false,\n" +
            "\"backchannel_logout_supported\": true,\n" +
            "\"backchannel_logout_session_supported\": false\n" +
            "}\n"

    fun getJwksJsonResponse(): String =
        "{\n" +
            "\"keys\": [\n" +
            "{\n" +
            "\"kty\": \"EC\",\n" +
            "\"use\": \"sig\",\n" +
            "\"crv\": \"P-256\",\n" +
            "\"kid\": \"6a4bc1e3-9530-4d5b-90c5-10dcf3ffccd0\",\n" +
            "\"x\": \"${ecKey.toPublicJWK().x}\",\n" +
            "\"y\": \"${ecKey.toPublicJWK().y}\",\n" +
            "\"alg\": \"ES256\"\n" +
            "},\n" +
            "{\n" +
            "\"kty\": \"EC\",\n" +
            "\"use\": \"sig\",\n" +
            "\"crv\": \"P-256\",\n" +
            "\"kid\": \"644af598b780f54106ca0f3c017341bc230c4f8373f35f32e18e3e40cc7acff6\",\n" +
            "\"x\": \"5URVCgH4HQgkg37kiipfOGjyVft0R5CdjFJahRoJjEw\",\n" +
            "\"y\": \"QzrvsnDy3oY1yuz55voaAq9B1M5tfhgW3FBjh_n_F0U\",\n" +
            "\"alg\": \"ES256\"\n" +
            "},\n" +
            "{\n" +
            "\"kty\": \"EC\",\n" +
            "\"use\": \"sig\",\n" +
            "\"crv\": \"P-256\",\n" +
            "\"kid\": \"e1f5699d068448882e7866b49d24431b2f21bf1a8f3c2b2dde8f4066f0506f1b\",\n" +
            "\"x\": \"BJnIZvnzJ9D_YRu5YL8a3CXjBaa5AxlX1xSeWDLAn9k\",\n" +
            "\"y\": \"x4FU3lRtkeDukSWVJmDuw2nHVFVIZ8_69n4bJ6ik4bQ\",\n" +
            "\"alg\": \"ES256\"\n" +
            "}\n" +
            "]\n" +
            "}"

    fun getTokenResponse(): String {
        val idToken: String = getIdToken()

        val responseBody =
            "{\n" +
                "\"access_token\": \"SlAV32hkKG\",\n" +
                "\"token_type\": \"Bearer\",\n" +
                "\"expires_in\": 180,\n" +
                "\"id_token\": \"$idToken\"\n" +
                "}"

        return responseBody
    }

    fun getUserInfoResponse(): String =
        "{\n" +
            "  \"sub\": \"$userId\",\n" +
            "  \"email\": \"$userEmail\",\n" +
            "  \"email_verified\": true,\n" +
            "  \"phone_number\": \"$userNumber\",\n" +
            "  \"phone_number_verified\": true\n" +
            "}"
}
