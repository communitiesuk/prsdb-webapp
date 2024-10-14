package uk.gov.communities.prsdb.webapp.local.api.mockOneLogin

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
    private var helper: MockOneLoginHelper = MockOneLoginHelper(),
) {
    @GetMapping("/.well-known/openid-configuration")
    fun openidConfiguration(): String = helper.getOpenidConfigurationResponse()

    @GetMapping("/.well-known/jwks.json")
    fun jwksJson(): String = helper.getJwksJsonResponse()

    @GetMapping("/authorize")
    fun authorize(
        @RequestParam response_type: String,
        @RequestParam client_id: String,
        @RequestParam scope: String,
        @RequestParam state: String,
        @RequestParam redirect_uri: String,
        @RequestParam nonce: String,
    ): ResponseEntity<Unit> {
        helper.lastReceivedNonce = nonce
        val locationURI: URI = URI.create("$redirect_uri?code=${helper.authorizationCode}&state=$state")

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
        val responseBody = helper.getTokenResponse()

        val responseBuild = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)

        return responseBuild
    }

    @GetMapping("/userinfo")
    fun userInfo(): ResponseEntity<String> {
        val responseBody = helper.getUserInfoResponse()

        val responseBuild = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(responseBody)

        return responseBuild
    }
}
