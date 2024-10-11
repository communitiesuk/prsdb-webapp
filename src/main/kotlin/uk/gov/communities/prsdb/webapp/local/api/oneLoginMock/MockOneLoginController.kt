package uk.gov.communities.prsdb.webapp.local.api.oneLoginMock

import org.springframework.context.annotation.Profile
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@Profile("local")
@RestController
@RequestMapping("/one-login-local")
class MockOneLoginController(
    private var jwtBuilder: JWTBuilder = JWTBuilder(),
) {
    @GetMapping("/.well-known/openid-configuration")
    fun example(): String =
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

    @GetMapping("/.well-known/jwks.json")
    fun jwksjson(): String =
        "{\n" +
            "\"keys\": [\n" +
            "{\n" +
            "\"kty\": \"EC\",\n" +
            "\"use\": \"sig\",\n" +
            "\"crv\": \"P-256\",\n" +
            "\"kid\": \"6a4bc1e3-9530-4d5b-90c5-10dcf3ffccd0\",\n" +
            "\"x\": \"${JWTBuilder.ecKey.toPublicJWK().x}\",\n" +
            "\"y\": \"${JWTBuilder.ecKey.toPublicJWK().y}\",\n" +
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

    @GetMapping("/authorize")
    fun authorize(
        @RequestParam response_type: String,
        @RequestParam client_id: String,
        @RequestParam scope: String,
        @RequestParam state: String,
        @RequestParam redirect_uri: String,
        @RequestParam nonce: String,
    ): ResponseEntity<Unit> {
        LastReceivedNonce.nonce = nonce
        val authorizationCode = "SplxlOBeZQQYbYS6WxSbIA"
        val locationURI: URI = URI.create("$redirect_uri?code=$authorizationCode&state=$state")

        return ResponseEntity.status(302).location(locationURI).build()
    }

    @PostMapping("/token")
    fun token(
        @RequestParam grant_type: String,
        @RequestParam redirect_uri: String,
        @RequestParam client_assertion: String,
        @RequestParam client_assertion_type: String,
        @RequestParam code: String,
    ): ResponseEntity<String> {
        // TODO data send to the jwtbuilder - subject: useridentifier, sid: session identifier

        val idToken: String = jwtBuilder.getIdToken()

        val responseBody =
            "{\n" +
                "\"access_token\": \"SlAV32hkKG\",\n" +
                "\"token_type\": \"Bearer\",\n" +
                "\"expires_in\": 180,\n" +
                "\"id_token\": \"$idToken\"\n" +
                "}"

        val responseBuild = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)

        return responseBuild
    }

    @GetMapping("/connect/register")
    fun example4(): String = "Hello World"

    @GetMapping("/trustmark")
    fun example5(): String =
        "{\n" +
            "\"idp\": \"http://localhost:8080//one-login-local/\",\n" +
            "\"trustmark_provider\": \"http://localhost:8080//one-login-local/\",\n" +
            "\"C\": [\n" +
            "\"Cl\",\n" +
            "\"Cl.Cm\"\n" +
            "],\n" +
            "\"P\": [\n" +
            "\"P0\",\n" +
            "\"PCL200\",\n" +
            "\"PCL250\",\n" +
            "\"P1\",\n" +
            "\"P2\"\n" +
            "]\n" +
            "}"

    @GetMapping("/logout")
    fun example6(): String = "Hello World"

    @GetMapping("/userinfo")
    fun example7(): ResponseEntity<String> {
        val responseBody =
            "{\n" +
                "  \"sub\": \"urn:fdc:gov.uk:2022:ABCDE\",\n" +
                "  \"email\": \"test@example.com\",\n" +
                "  \"email_verified\": true,\n" +
                "  \"phone_number\": \"01406946277\",\n" +
                "  \"phone_number_verified\": true\n" +
                "}"

        val responseBuild = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)

        return responseBuild
    }
}
