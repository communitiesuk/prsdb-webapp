package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IdentityVerificationContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `identity verification sends claims and synthetic identity then resumes the verified journey`() {
        IdentityVerificationServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(
                listOf(
                    "/registration/verify-identity?journeyId=synthetic",
                    "/id-verification/oauth2/authorize/one-login",
                    "/login/oauth2/code/one-login?code=identity-code&state=identity-state",
                    "/registration/verify-identity?journeyId=synthetic",
                ),
                server.appRequests.map { it.path },
            )
            assertEquals(listOf("/authorize", "/form-submit"), server.simulatorRequests.map { it.path.substringBefore("?") })
            assertEquals("""["Cl.Cm.P2"]""", server.authorizationParameters["vtr"])
            assertTrue(server.authorizationParameters["claims"].orEmpty().contains(CORE_IDENTITY_CLAIM))
            assertTrue(server.authorizationParameters["claims"].orEmpty().contains(ADDRESS_CLAIM))
            assertTrue(server.authorizationParameters["claims"].orEmpty().contains(RETURN_CODE_CLAIM))
            assertTrue(server.submittedFields["coreIdentity"].orEmpty().contains("ALEXANDER"))
            assertTrue(server.submittedFields["address"].orEmpty().contains("SW1A 1AA"))
            assertTrue(server.submittedFields["returnCodes"].orEmpty().contains("00"))
            assertTrue(server.appRequests.toList()[2].cookie.orEmpty().contains("APP_SESSION=landlord-session"))
            assertTrue(server.appRequests.toList()[2].cookie.orEmpty().contains("IDV_STATE=identity-flow"))
            assertFalse(server.simulatorRequests.first().cookie.orEmpty().contains("APP_SESSION="))
            assertEquals("/registration/confirm-identity?journeyId=synthetic", server.resumedLocation)
        }
    }

    @Test
    fun `identity verification without a core identity resumes the unverified journey`() {
        IdentityVerificationServer().use { server ->
            val result = runSimulation(server, mapOf("gatling.contract.identityUnverified" to "true"))

            assertEquals(0, result.exitCode, result.output)
            assertEquals("/registration/identity-not-verified?journeyId=synthetic", server.resumedLocation)
            assertEquals("", server.submittedFields["coreIdentity"])
        }
    }

    @Test
    fun `rejected identity verification does not reach the application callback`() {
        IdentityVerificationServer(rejectSubmission = true).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.appRequests.any { it.path.startsWith("/login/oauth2/code/one-login") })
            assertEquals(listOf("/authorize", "/form-submit"), server.simulatorRequests.map { it.path.substringBefore("?") })
        }
    }

    private fun runSimulation(
        server: IdentityVerificationServer,
        extraProperties: Map<String, String> = emptyMap(),
    ): GatlingTestRunner.Result {
        val properties =
            mapOf(
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.baseUrl" to server.appUrl,
                "gatling.simulatorUrl" to server.simulatorUrl,
                "gatling.basic.landlordSubject" to "synthetic-registration-subject",
            ) + extraProperties
        return GatlingTestRunner(outputDirectory).run(IdentityVerificationContractSimulation::class.java, properties)
    }

    private class IdentityVerificationServer(
        private val rejectSubmission: Boolean = false,
    ) : AutoCloseable {
        data class Request(
            val path: String,
            val cookie: String?,
        )

        private val appServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        private val simulatorServer = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        val appUrl: String
            get() = "http://127.0.0.1:${appServer.address.port}"
        val simulatorUrl: String
            get() = "http://localhost:${simulatorServer.address.port}"
        val appRequests = ConcurrentLinkedQueue<Request>()
        val simulatorRequests = ConcurrentLinkedQueue<Request>()
        val authorizationParameters = ConcurrentHashMap<String, String>()
        @Volatile
        var submittedFields: Map<String, String> = emptyMap()
            private set
        @Volatile
        var resumedLocation: String? = null
            private set
        @Volatile
        private var verified = true

        init {
            appServer.createContext("/") { exchange ->
                val path = exchange.requestURI.toString()
                val cookie = exchange.requestHeaders.getFirst("Cookie")
                appRequests.add(Request(path, cookie))
                val (status, body) =
                    when (exchange.requestURI.path) {
                        "/registration/verify-identity" -> {
                            if (cookie?.contains("IDV_CALLBACK=complete") == true) {
                                resumedLocation =
                                    if (verified) {
                                        "/registration/confirm-identity?journeyId=synthetic"
                                    } else {
                                        "/registration/identity-not-verified?journeyId=synthetic"
                                    }
                                exchange.responseHeaders.add("Location", resumedLocation)
                                302 to ""
                            } else {
                                exchange.responseHeaders.add("Set-Cookie", "APP_SESSION=landlord-session; Path=/; HttpOnly")
                                exchange.responseHeaders.add("Location", "/id-verification/oauth2/authorize/one-login")
                                302 to ""
                            }
                        }
                        "/id-verification/oauth2/authorize/one-login" -> {
                            if (cookie?.contains("APP_SESSION=landlord-session") != true) {
                                401 to "Missing landlord session"
                            } else {
                                exchange.responseHeaders.add("Set-Cookie", "IDV_STATE=identity-flow; Path=/; HttpOnly")
                                exchange.responseHeaders.add(
                                    "Location",
                                    "$simulatorUrl/authorize?vtr=%5B%22Cl.Cm.P2%22%5D&claims=$ENCODED_CLAIMS",
                                )
                                302 to ""
                            }
                        }
                        "/login/oauth2/code/one-login" -> {
                            if (
                                cookie?.contains("APP_SESSION=landlord-session") == true &&
                                cookie.contains("IDV_STATE=identity-flow")
                            ) {
                                exchange.responseHeaders.add("Set-Cookie", "IDV_CALLBACK=complete; Path=/; HttpOnly")
                                exchange.responseHeaders.add(
                                    "Location",
                                    "/registration/verify-identity?journeyId=synthetic",
                                )
                                302 to ""
                            } else {
                                403 to "Missing application identity-verification session"
                            }
                        }
                        else -> 404 to "Not found"
                    }
                respond(exchange, status, body, "text/html")
            }
            simulatorServer.createContext("/") { exchange ->
                val path = exchange.requestURI.toString()
                val cookie = exchange.requestHeaders.getFirst("Cookie")
                simulatorRequests.add(Request(path, cookie))
                val (status, body) =
                    when (exchange.requestURI.path) {
                        "/authorize" -> {
                            exchange.requestURI.rawQuery.orEmpty().split("&").forEach { entry ->
                                val parts = entry.split("=", limit = 2)
                                authorizationParameters[parts[0]] =
                                    URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                            }
                            exchange.responseHeaders.add("Set-Cookie", "SIM_SESSION=identity-flow; Path=/; HttpOnly")
                            200 to """
                                <form method="post" action="$simulatorUrl/form-submit">
                                  <input type="hidden" name="authCode" value="identity-code">
                                  <input type="hidden" name="authRequestParams" value="encoded-request">
                                  <input type="hidden" name="state" value="identity-state">
                                  <input data-testid="sub" name="sub" value="default-subject">
                                  <textarea data-testid="core-identity-vc" name="coreIdentity"></textarea>
                                  <textarea data-testid="postal-address-details" name="address"></textarea>
                                  <textarea data-testid="return-codes" name="returnCodes"></textarea>
                                  <button name="continue" value="continue">Continue</button>
                                </form>
                                """.trimIndent()
                        }
                        "/form-submit" -> {
                            submittedFields = parseForm(exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8))
                            verified = submittedFields["coreIdentity"].orEmpty().isNotBlank()
                            if (cookie?.contains("SIM_SESSION=identity-flow") != true) {
                                403 to "Missing simulator session"
                            } else if (rejectSubmission) {
                                403 to "Identity verification rejected"
                            } else {
                                exchange.responseHeaders.add(
                                    "Location",
                                    "$appUrl/login/oauth2/code/one-login?code=identity-code&state=identity-state",
                                )
                                302 to ""
                            }
                        }
                        else -> 404 to "Not found"
                    }
                respond(exchange, status, body, if (exchange.requestURI.path == "/authorize") "text/html" else "text/plain")
            }
            appServer.start()
            simulatorServer.start()
        }

        override fun close() {
            appServer.stop(0)
            simulatorServer.stop(0)
        }

        private fun parseForm(body: String): Map<String, String> =
            body.split("&").associate {
                val parts = it.split("=", limit = 2)
                URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            }

        private fun respond(
            exchange: com.sun.net.httpserver.HttpExchange,
            status: Int,
            body: String,
            contentType: String,
        ) {
            val bytes = body.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "$contentType; charset=utf-8")
            exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1 else bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        companion object {
            private val ENCODED_CLAIMS =
                java.net.URLEncoder
                    .encode(
                        """{"userinfo":{"$CORE_IDENTITY_CLAIM":null,"$ADDRESS_CLAIM":null,"$RETURN_CODE_CLAIM":null}}""",
                        StandardCharsets.UTF_8,
                    )
        }
    }

    companion object {
        private const val CORE_IDENTITY_CLAIM = "https://vocab.account.gov.uk/v1/coreIdentityJWT"
        private const val ADDRESS_CLAIM = "https://vocab.account.gov.uk/v1/address"
        private const val RETURN_CODE_CLAIM = "https://vocab.account.gov.uk/v1/returnCode"
    }
}
