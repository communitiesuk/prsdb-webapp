package uk.gov.communities.prsdb.webapp.local.api.helper

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
import java.util.UUID

class MockOneLoginHelper {
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
    }

    private val userId = "urn:fdc:gov.uk:2022:UVWXY"

    // These values are from One-Login's publicly available docs (https://docs.sign-in.service.gov.uk/integrate-with-integration-environment/authenticate-your-user/)
    private val userEmail = "test@example.com"
    private val userNumber = "01406946277"
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
                .claim("sid", "dX5xv0XgHh6yfD1xy-ss_1EDK0I")

        val signedJwt = SignedJWT(headerBuilder.build(), claimSetBBuilder.build())

        signedJwt.sign(ECDSASigner(ecKey))

        return signedJwt.serialize()
    }

    fun getJwksJsonResponse(): String =
        "{\n" +
            "\"keys\": [\n" +
            "{\n" +
            "\"kty\": \"EC\",\n" +
            "\"use\": \"sig\",\n" +
            "\"crv\": \"P-256\",\n" +
            "\"kid\": \"$keyId\",\n" +
            "\"x\": \"${ecKey.toPublicJWK().x}\",\n" +
            "\"y\": \"${ecKey.toPublicJWK().y}\",\n" +
            "\"alg\": \"ES256\"\n" +
            "}" +
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
}
