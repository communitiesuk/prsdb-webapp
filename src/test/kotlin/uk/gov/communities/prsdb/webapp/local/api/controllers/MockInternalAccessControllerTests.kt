package uk.gov.communities.prsdb.webapp.local.api.controllers

import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jwt.SignedJWT
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor
import org.springframework.boot.context.properties.bind.Bindable
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientProperties
import org.springframework.boot.security.oauth2.client.autoconfigure.OAuth2ClientPropertiesMapper
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.SystemEnvironmentPropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.env.MockEnvironment
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.util.UriComponentsBuilder
import java.net.InetSocketAddress
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.util.Base64
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MockInternalAccessControllerTests {
    private val route = "/local/internal-access"
    private val clientId = "902e8a06-4086-423d-8e67-3838c4e52d7f"
    private val secret = "synthetic-secret"
    private val origin = "https://app.example.test"
    private val callback = "$origin/local-council/login/oauth2/code/internal-access"

    private class MutableClock(
        var now: Instant = Instant.parse("2030-01-01T00:00:00Z"),
    ) : Clock() {
        override fun getZone() = ZoneOffset.UTC

        override fun withZone(zone: java.time.ZoneId?) = this

        override fun instant(): Instant = now
    }

    @Test
    fun `performance nft registration never discovers the environment supplied council issuer`() {
        val requests = AtomicInteger()
        val canary = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        canary.createContext("/") { exchange ->
            requests.incrementAndGet()
            exchange.sendResponseHeaders(404, -1)
            exchange.close()
        }
        canary.start()
        try {
            val realIssuer = "http://127.0.0.1:${canary.address.port}"
            val properties = councilProperties(realIssuer, "default", "performance-test", "nft")
            assertEquals(realIssuer, properties.provider.getValue("internal-access").issuerUri)

            val registration = OAuth2ClientPropertiesMapper(properties).asClientRegistrations().getValue("internal-access")

            assertEquals(0, requests.get())
            assertEquals("http://localhost:8080/local/internal-access/authorize", registration.providerDetails.authorizationUri)
            assertEquals("http://localhost:8080/local/internal-access/token", registration.providerDetails.tokenUri)
            assertEquals("http://localhost:8080/local/internal-access/.well-known/jwks.json", registration.providerDetails.jwkSetUri)
            assertEquals("http://localhost:8080/local/internal-access/userinfo", registration.providerDetails.userInfoEndpoint.uri)
            assertEquals("sub", registration.providerDetails.userInfoEndpoint.userNameAttributeName)
            assertEquals(null, registration.providerDetails.issuerUri)
            assertEquals(clientId, registration.clientId)
            assertEquals("client_secret_basic", registration.clientAuthenticationMethod.value)
            assertEquals(
                "http://localhost:8080/local-council/login/oauth2/code/internal-access",
                registration.redirectUri,
            )
            assertTrue("openid" in registration.scopes)

            // Prove the canary detects the ordinary provider's discovery path rather than masking outbound calls.
            val ordinaryProperties = councilProperties(realIssuer, "default")
            assertFailsWith<IllegalArgumentException> {
                OAuth2ClientPropertiesMapper(ordinaryProperties).asClientRegistrations()
            }
            assertTrue(requests.get() > 0)
        } finally {
            canary.stop(0)
        }
    }

    @Test
    fun `non nft and production council configuration retains its ordinary provider`() {
        listOf(
            arrayOf("default"),
            arrayOf("default", "performance-test"),
            arrayOf("default", "nft"),
            arrayOf("default", "performance-test", "nft", "production"),
        ).forEach { profiles ->
            val properties = councilProperties("http://127.0.0.1:1", *profiles)
            assertEquals(null, properties.registration.getValue("internal-access").provider)
            assertEquals("http://127.0.0.1:1", properties.provider.getValue("internal-access").issuerUri)
        }
    }

    private fun councilProperties(
        realIssuer: String,
        vararg profiles: String,
    ): OAuth2ClientProperties {
        val environment =
            MockEnvironment()
                .withProperty(
                    "spring.config.location",
                    "file:src/main/resources/application.yml,file:src/main/resources/application-performance-test.yml",
                )
                .withProperty("INTERNAL_ACCESS_CLIENT_ID", clientId)
                .withProperty("INTERNAL_ACCESS_CLIENT_SECRET", "synthetic-environment-secret")
        environment.setActiveProfiles(*profiles)
        environment.propertySources.addLast(
            SystemEnvironmentPropertySource("synthetic-environment", mapOf("INTERNAL_ACCESS_ISSUER_URL" to realIssuer)),
        )
        ConfigDataEnvironmentPostProcessor.applyTo(environment)
        val binder = Binder.get(environment)
        val prefix = "spring.security.oauth2.client"
        return OAuth2ClientProperties().also { properties ->
            // Bind the real configuration, isolating this provider from unrelated One Login and EPC registrations.
            properties.registration["internal-access"] =
                binder.bind("$prefix.registration.internal-access", Bindable.of(OAuth2ClientProperties.Registration::class.java)).get()
            listOf("internal-access", "internal-access-performance-test").forEach { provider ->
                binder.bind("$prefix.provider.$provider", Bindable.of(OAuth2ClientProperties.Provider::class.java))
                    .ifBound { properties.provider[provider] = it }
            }
        }
    }

    private val clock = MutableClock()

    private fun controller(vararg profiles: String) =
        MockInternalAccessController(
            "8080",
            MockEnvironment().also { it.setActiveProfiles(*profiles) },
            origin,
            secret,
        ).also {
            it.basePathComponent = ""
            it.clock = clock
        }

    private fun performanceMvc(): MockMvc = MockMvcBuilders.standaloneSetup(controller("performance-test", "nft")).build()

    private fun basic(
        id: String = clientId,
        password: String = secret,
    ) = "Basic " + Base64.getEncoder().encodeToString("$id:$password".toByteArray())

    private fun authorize(
        mvc: MockMvc,
        nonce: String,
        state: String = "state-$nonce",
        client: String = clientId,
        redirectUri: String = callback,
    ): MvcResult =
        mvc.perform(
            get("$route/authorize")
                .param("response_type", "code")
                .param("client_id", client)
                .param("scope", "openid email profile")
                .param("state", state)
                .param("redirect_uri", redirectUri)
                .param("nonce", nonce),
        ).andReturn()

    private fun hidden(
        html: String,
        name: String,
    ): String = Regex("""name="$name" value="([^"]*)"""").find(html)!!.groupValues[1]

    private fun submit(
        mvc: MockMvc,
        page: String,
        subject: String,
        state: String = hidden(page, "state"),
    ): MvcResult =
        mvc.perform(
            post("$route/authorize")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("authCode", hidden(page, "authCode"))
                .param("state", state)
                .param("authRequestParams", hidden(page, "authRequestParams"))
                .param("sub", subject)
                .param("email", "synthetic@example.test")
                .param("phoneNumber", "02079460123")
                .param("maxLoCAchieved", "P0")
                .param("continue", "continue"),
        ).andReturn()

    private fun codeFrom(result: MvcResult) =
        UriComponentsBuilder.fromUriString(result.response.getHeader("Location")!!).build().queryParams.getFirst("code")!!

    private fun exchange(
        mvc: MockMvc,
        code: String,
        authorization: String = basic(),
        redirectUri: String = callback,
    ): MvcResult =
        mvc.perform(
            post("$route/token")
                .header(HttpHeaders.AUTHORIZATION, authorization)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("grant_type", "authorization_code")
                .param("redirect_uri", redirectUri)
                .param("code", code),
        ).andReturn()

    private fun idToken(result: MvcResult): SignedJWT {
        val body = result.response.contentAsString
        val token = Regex(""""id_token"\s*:\s*"([^"]+)"""").find(body)!!.groupValues[1]
        val jwt = SignedJWT.parse(token)
        assertTrue(jwt.verify(RSASSAVerifier(MockInternalAccessController.rsaKey.toRSAPublicKey())))
        return jwt
    }

    @Test
    fun `local profile keeps the fixed user and immediate redirect`() {
        val mvc = MockMvcBuilders.standaloneSetup(controller("local")).build()

        val authorization =
            authorize(mvc, "local-nonce", redirectUri = "http://localhost:8080/local-council/login/oauth2/code/internal-access")
        assertEquals(302, authorization.response.status)
        assertTrue(authorization.response.getHeader("Location")!!.contains("code=MockInternalAccessCode&state=state-local-nonce"))

        val token =
            mvc.perform(
                post("$route/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("grant_type", "authorization_code")
                    .param("redirect_uri", "x")
                    .param("code", "MockInternalAccessCode"),
            ).andReturn()
        val claims = idToken(token).jwtClaimsSet
        assertEquals("ia-mock-user-12345", claims.subject)
        assertEquals("local-nonce", claims.getStringClaim("nonce"))
    }

    @Test
    fun `interleaved logins keep their own subject and nonce`() {
        val mvc = performanceMvc()
        val pageA = authorize(mvc, "nonce-a").response.contentAsString
        val pageB = authorize(mvc, "nonce-b").response.contentAsString
        assertNotEquals(hidden(pageA, "authCode"), hidden(pageB, "authCode"))

        val codeB = codeFrom(submit(mvc, pageB, "subject-b"))
        val codeA = codeFrom(submit(mvc, pageA, "subject-a"))

        val tokenA = idToken(exchange(mvc, codeA)).jwtClaimsSet
        val tokenB = idToken(exchange(mvc, codeB)).jwtClaimsSet
        assertEquals("subject-a", tokenA.subject)
        assertEquals("nonce-a", tokenA.getStringClaim("nonce"))
        assertEquals("subject-b", tokenB.subject)
        assertEquals("nonce-b", tokenB.getStringClaim("nonce"))
        assertEquals(listOf(clientId), tokenA.audience)
        assertEquals("$origin/local/internal-access", tokenA.issuer)
    }

    @Test
    fun `a code is consumed once and cannot be replayed`() {
        val mvc = performanceMvc()
        val code = codeFrom(submit(mvc, authorize(mvc, "n").response.contentAsString, "subject-a"))

        assertEquals(200, exchange(mvc, code).response.status)
        assertEquals(400, exchange(mvc, code).response.status)
    }

    @Test
    fun `wrong client or secret is rejected at authorize and token`() {
        val mvc = performanceMvc()
        assertEquals(400, authorize(mvc, "n", client = "other-client").response.status)

        val code = codeFrom(submit(mvc, authorize(mvc, "n").response.contentAsString, "subject-a"))
        assertEquals(401, exchange(mvc, code, basic(password = "wrong")).response.status)
        assertEquals(401, exchange(mvc, code, basic(id = "other-client")).response.status)
        assertEquals(200, exchange(mvc, code).response.status)
    }

    @Test
    fun `invalid callbacks are rejected without redirecting`() {
        val mvc = performanceMvc()
        listOf(
            "https://evil.example.test/local-council/login/oauth2/code/internal-access",
            "$origin/login/oauth2/code/one-login",
            "$callback?extra=1",
            "$callback#fragment",
        ).forEach {
            val result = authorize(mvc, "n", redirectUri = it)
            assertEquals(400, result.response.status, it)
            assertEquals(null, result.response.getHeader("Location"))
        }

        val code = codeFrom(submit(mvc, authorize(mvc, "n").response.contentAsString, "subject-a"))
        assertEquals(400, exchange(mvc, code, redirectUri = "$origin/other").response.status)
    }

    @Test
    fun `mismatched state or unknown code cannot complete a login`() {
        val mvc = performanceMvc()
        val page = authorize(mvc, "n").response.contentAsString

        assertEquals(400, submit(mvc, page, "subject-a", state = "different").response.status)
        assertEquals(400, exchange(mvc, "unknown").response.status)
        // an unsubmitted authorization has no identity to exchange
        assertEquals(400, exchange(mvc, hidden(page, "authCode")).response.status)
    }

    @Test
    fun `codes expire`() {
        val mvc = performanceMvc()
        val code = codeFrom(submit(mvc, authorize(mvc, "n").response.contentAsString, "subject-a"))

        clock.now = clock.now.plus(Duration.ofMinutes(10))
        assertEquals(400, exchange(mvc, code).response.status)
    }

    @Test
    fun `subject form input is html escaped and blank subjects are refused`() {
        val mvc = performanceMvc()
        val page = authorize(mvc, "n", state = "\"><script>").response.contentAsString
        assertFalse(page.contains("<script>"))

        val clean = authorize(mvc, "n").response.contentAsString
        assertEquals(400, submit(mvc, clean, " ").response.status)
    }

    @Test
    fun `discovery and jwks stay public in performance mode`() {
        val mvc = performanceMvc()
        mvc.perform(get("$route/.well-known/jwks.json")).andExpect(status().isOk)
        mvc.perform(get("$route/.well-known/openid-configuration")).andExpect(status().isOk)
            .andExpect(header().string("Content-Type", "application/json"))
    }

    @Test
    fun `controller is only registered for local or non-production nft performance-test profiles`() {
        mapOf(
            arrayOf("local") to true,
            arrayOf("performance-test", "nft") to true,
            arrayOf("performance-test") to false,
            arrayOf("nft") to false,
            arrayOf("performance-test", "nft", "production") to false,
            arrayOf<String>() to false,
        ).forEach { (profiles, enabled) ->
            AnnotationConfigApplicationContext().use { context ->
                context.environment.setActiveProfiles(*profiles)
                context.register(MockInternalAccessController::class.java)
                context.refresh()
                assertEquals(
                    enabled,
                    context.getBeansOfType(MockInternalAccessController::class.java).isNotEmpty(),
                    profiles.joinToString(),
                )
            }
        }
    }

    @Test
    fun `performance configuration points the council provider at the mock`() {
        val environment = MockEnvironment()
        YamlPropertySourceLoader().load("performance-test", ClassPathResource("application-performance-test.yml"))
            .forEach { environment.propertySources.addFirst(it) }
        val prefix = "spring.security.oauth2.client"

        assertEquals(
            "http://localhost:8080/local/internal-access/token",
            environment.getRequiredProperty("$prefix.provider.internal-access-performance-test.token-uri"),
        )
        assertEquals(
            "http://localhost:8080/local/internal-access/authorize",
            environment.getRequiredProperty("$prefix.provider.internal-access-performance-test.authorization-uri"),
        )
        assertEquals(
            "http://localhost:8080/local-council/login/oauth2/code/internal-access",
            environment.getRequiredProperty("$prefix.registration.internal-access.redirect-uri"),
        )
        assertEquals("performance-test", environment.getRequiredProperty("$prefix.registration.internal-access.client-secret"))
    }
}
