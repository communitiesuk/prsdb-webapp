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
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.HtmlUtils
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbRestController
import java.net.URI
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID

// The performance-test behaviour is opt-in and never active in production; plain local behaviour is unchanged.
@Profile("local | (performance-test & nft & !production)")
@PrsdbRestController
@RequestMapping("/local/internal-access")
class MockInternalAccessController(
    @Value("\${server.port:8080}") private val serverPort: String,
    private val environment: Environment,
    @Value("\${performance-test.internal-access.public-origin:http://localhost:\${server.port:8080}}")
    private val publicOrigin: String,
    @Value("\${performance-test.internal-access.client-secret:performance-test}")
    private val clientSecret: String,
) {
    companion object {
        val keyId: String = UUID.randomUUID().toString()

        val rsaKey: RSAKey =
            RSAKeyGenerator(2048)
                .keyUse(KeyUse.SIGNATURE)
                .keyID(keyId)
                .algorithm(JWSAlgorithm.RS256)
                .generate()

        private const val CALLBACK_PATH = "/local-council/login/oauth2/code/internal-access"
        private const val CODE_LIFETIME_SECONDS = 300L
        private const val MAX_PENDING = 10_000
        private const val MAX_VALUE_LENGTH = 1024
        private const val MAX_SUBJECT_LENGTH = 128
    }

    private val userId = "ia-mock-user-12345"
    private val userEmail = "test@example.com"
    private val userName = "Mock User"
    private val clientId = "902e8a06-4086-423d-8e67-3838c4e52d7f"

    @Value("\${local.base-path-component:}")
    lateinit var basePathComponent: String

    var lastReceivedNonce: String? = null

    internal var clock: Clock = Clock.systemUTC()

    private class Identity(
        val subject: String,
        val email: String,
        val phoneNumber: String,
        val emailVerified: Boolean,
        val phoneNumberVerified: Boolean,
    )

    private class Authorization(
        val redirectUri: String,
        val state: String,
        val nonce: String,
        val requestId: String,
        val expiresAt: Instant,
        var identity: Identity? = null,
    )

    private class AccessGrant(
        val identity: Identity,
        val expiresAt: Instant,
    )

    private val performanceMode = environment.acceptsProfiles(Profiles.of("performance-test & nft & !production"))
    private val lock = Any()
    private val authorizations = HashMap<String, Authorization>()
    private val accessGrants = HashMap<String, AccessGrant>()

    private val issuerBaseUrl get() = if (performanceMode) "${publicOrigin.trimEnd('/')}/local/internal-access" else loopbackUrl
    private val loopbackUrl get() = "http://localhost:$serverPort/local/internal-access"

    @GetMapping("/.well-known/openid-configuration")
    fun openidConfiguration(): ResponseEntity<String> {
        val baseUrl = loopbackUrl
        val config =
            """
            {
                "issuer": "$issuerBaseUrl",
                "authorization_endpoint": "$issuerBaseUrl/authorize",
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
    ): ResponseEntity<String> {
        if (performanceMode) return performanceAuthorize(response_type, client_id, state, redirect_uri, nonce)

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

    @PostMapping("/authorize")
    fun submitAuthorization(
        @RequestParam authCode: String,
        @RequestParam state: String,
        @RequestParam authRequestParams: String,
        @RequestParam sub: String,
        @RequestParam(defaultValue = "") email: String,
        @RequestParam(defaultValue = "") phoneNumber: String,
        @RequestParam(required = false) emailVerified: String?,
        @RequestParam(required = false) phoneNumberVerified: String?,
        @RequestParam(required = false) phoneVerified: String?,
    ): ResponseEntity<String> {
        if (!performanceMode) return ResponseEntity.notFound().build()
        val subject = sub.trim()
        if (subject.isEmpty() || subject.length > MAX_SUBJECT_LENGTH || subject.any { it.isISOControl() } ||
            email.length > MAX_VALUE_LENGTH || phoneNumber.length > MAX_VALUE_LENGTH
        ) {
            return badRequest()
        }
        val identity =
            Identity(
                subject,
                email,
                phoneNumber,
                emailVerified.toBoolean(),
                (phoneNumberVerified ?: phoneVerified).toBoolean(),
            )

        val authorization =
            synchronized(lock) {
                val pending = authorizations[authCode]
                if (pending == null || pending.identity != null || pending.isExpired() ||
                    !constantTimeEquals(pending.state, state) || !constantTimeEquals(pending.requestId, authRequestParams)
                ) {
                    return badRequest()
                }
                pending.identity = identity
                pending
            }

        val location =
            UriComponentsBuilder
                .fromUriString(authorization.redirectUri)
                .queryParam("code", authCode)
                .queryParam("state", authorization.state)
                .build()
                .encode()
                .toUri()
        return ResponseEntity.status(HttpStatus.FOUND).location(location).build()
    }

    @PostMapping("/token")
    fun token(
        @RequestParam grant_type: String,
        @RequestParam redirect_uri: String,
        @RequestParam code: String,
        @RequestParam(required = false) client_id: String?,
        @RequestParam(required = false) client_secret: String?,
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
    ): ResponseEntity<Any> {
        if (performanceMode) return performanceToken(grant_type, redirect_uri, code, client_id, client_secret, authorization)

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
    fun userInfo(
        @RequestHeader(HttpHeaders.AUTHORIZATION, required = false) authorization: String?,
    ): ResponseEntity<Any> {
        if (performanceMode) return performanceUserInfo(authorization)

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

    private fun performanceAuthorize(
        responseType: String,
        clientId: String,
        state: String,
        redirectUri: String,
        nonce: String,
    ): ResponseEntity<String> {
        if (responseType != "code" || clientId != this.clientId || !isValidCallback(redirectUri) ||
            nonce.isBlank() || nonce.length > MAX_VALUE_LENGTH || state.length > MAX_VALUE_LENGTH
        ) {
            return badRequest()
        }

        val code = UUID.randomUUID().toString()
        val requestId = UUID.randomUUID().toString()
        synchronized(lock) {
            evictExpired()
            if (authorizations.size >= MAX_PENDING) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
            }
            authorizations[code] =
                Authorization(redirectUri, state, nonce, requestId, clock.instant().plusSeconds(CODE_LIFETIME_SECONDS))
        }

        return ResponseEntity
            .ok()
            .contentType(MediaType.TEXT_HTML)
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .body(loginForm(code, state, requestId))
    }

    private fun performanceToken(
        grantType: String,
        redirectUri: String,
        code: String,
        clientIdParameter: String?,
        clientSecretParameter: String?,
        authorizationHeader: String?,
    ): ResponseEntity<Any> {
        val credentials = clientCredentials(authorizationHeader, clientIdParameter, clientSecretParameter)
        if (credentials == null || !constantTimeEquals(credentials.first, clientId) ||
            !constantTimeEquals(credentials.second, clientSecret)
        ) {
            return tokenError(HttpStatus.UNAUTHORIZED, "invalid_client")
        }
        if (grantType != "authorization_code") return tokenError(HttpStatus.BAD_REQUEST, "unsupported_grant_type")

        val authorization = synchronized(lock) { authorizations.remove(code) }
        val identity = authorization?.identity
        if (authorization == null || identity == null || authorization.isExpired() ||
            !constantTimeEquals(authorization.redirectUri, redirectUri)
        ) {
            return tokenError(HttpStatus.BAD_REQUEST, "invalid_grant")
        }

        val accessToken = UUID.randomUUID().toString()
        val expiresAt = clock.instant().plusSeconds(CODE_LIFETIME_SECONDS)
        synchronized(lock) {
            if (accessGrants.size >= MAX_PENDING) return tokenError(HttpStatus.SERVICE_UNAVAILABLE, "temporarily_unavailable")
            accessGrants[accessToken] = AccessGrant(identity, expiresAt)
        }

        val body =
            mapOf(
                "access_token" to accessToken,
                "token_type" to "Bearer",
                "expires_in" to CODE_LIFETIME_SECONDS,
                "id_token" to signIdToken(identity.subject, issuerBaseUrl, authorization.nonce, identityClaims(identity)),
            )
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store").body(body)
    }

    private fun performanceUserInfo(authorizationHeader: String?): ResponseEntity<Any> {
        val token = authorizationHeader?.takeIf { it.startsWith("Bearer ") }?.substring("Bearer ".length)
        val grant = synchronized(lock) { token?.let { accessGrants[it] } }
        if (grant == null || !grant.expiresAt.isAfter(clock.instant())) return tokenError(HttpStatus.UNAUTHORIZED, "invalid_token")
        return ResponseEntity.ok(mapOf("sub" to grant.identity.subject) + identityClaims(grant.identity))
    }

    private fun identityClaims(identity: Identity) =
        mapOf(
            "email" to identity.email,
            "name" to userName,
            "email_verified" to identity.emailVerified,
            "phone_number" to identity.phoneNumber,
            "phone_number_verified" to identity.phoneNumberVerified,
        )

    private fun loginForm(
        code: String,
        state: String,
        requestId: String,
    ): String {
        fun hidden(
            name: String,
            value: String,
        ) = """<input type="hidden" name="$name" value="${HtmlUtils.htmlEscape(value)}">"""
        return """
            <!DOCTYPE html>
            <html lang="en"><head><title>Internal access sign in</title></head><body>
            <form method="post" action="${HtmlUtils.htmlEscape("$basePathComponent/local/internal-access/authorize")}">
            ${hidden("authCode", code)}
            ${hidden("state", state)}
            ${hidden("authRequestParams", requestId)}
            <label>Subject <input name="sub" value=""></label>
            <label>Email <input name="email" value="synthetic@example.test"></label>
            <label>Phone <input name="phoneNumber" value="02079460123"></label>
            <label>Confidence <input name="maxLoCAchieved" value="P2"></label>
            <input type="checkbox" name="emailVerified" value="true" checked>
            <input type="checkbox" name="phoneNumberVerified" value="true" checked>
            <button type="submit" name="continue" value="continue">Continue</button>
            </form></body></html>
            """.trimIndent()
    }

    private fun isValidCallback(redirectUri: String): Boolean {
        val uri = runCatching { URI.create(redirectUri) }.getOrNull() ?: return false
        return uri.scheme != null && uri.rawAuthority != null && "${uri.scheme}://${uri.rawAuthority}" == publicOrigin.trimEnd('/') &&
            uri.rawPath == CALLBACK_PATH && uri.rawQuery == null && uri.rawFragment == null && uri.rawUserInfo == null
    }

    private fun clientCredentials(
        authorizationHeader: String?,
        clientIdParameter: String?,
        clientSecretParameter: String?,
    ): Pair<String, String>? {
        if (authorizationHeader != null) {
            if (!authorizationHeader.startsWith("Basic ")) return null
            val decoded =
                runCatching {
                    String(Base64.getDecoder().decode(authorizationHeader.substring("Basic ".length)), StandardCharsets.UTF_8)
                }.getOrNull() ?: return null
            val separator = decoded.indexOf(':')
            return if (separator < 0) null else decoded.substring(0, separator) to decoded.substring(separator + 1)
        }
        return if (clientIdParameter != null && clientSecretParameter != null) clientIdParameter to clientSecretParameter else null
    }

    private fun Authorization.isExpired() = !expiresAt.isAfter(clock.instant())

    private fun evictExpired() {
        val now = clock.instant()
        authorizations.values.removeIf { !it.expiresAt.isAfter(now) }
        accessGrants.values.removeIf { !it.expiresAt.isAfter(now) }
    }

    private fun constantTimeEquals(
        first: String,
        second: String,
    ) = MessageDigest.isEqual(first.toByteArray(StandardCharsets.UTF_8), second.toByteArray(StandardCharsets.UTF_8))

    private fun badRequest() = ResponseEntity.badRequest().contentType(MediaType.TEXT_PLAIN).body("invalid_request")

    private fun tokenError(
        status: HttpStatus,
        error: String,
    ) = ResponseEntity.status(status).body<Any>(mapOf("error" to error))

    private fun getIdToken(): String =
        signIdToken(
            userId,
            loopbackUrl,
            lastReceivedNonce,
            mapOf("email" to userEmail, "name" to userName),
        )

    private fun signIdToken(
        subject: String,
        issuer: String,
        nonce: String?,
        extraClaims: Map<String, Any>,
    ): String {
        val header =
            JWSHeader
                .Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .keyID(keyId)
                .build()

        val builder =
            JWTClaimsSet
                .Builder()
                .subject(subject)
                .audience(clientId)
                .issuer(issuer)
                .issueTime(Date.from(clock.instant()))
                .expirationTime(Date.from(clock.instant().plusSeconds(CODE_LIFETIME_SECONDS)))
                .claim("nonce", nonce)
        extraClaims.forEach { (name, value) -> builder.claim(name, value) }
        val claimsSet = builder.build()

        val signedJwt = SignedJWT(header, claimsSet)
        signedJwt.sign(RSASSASigner(rsaKey))

        return signedJwt.serialize()
    }
}
