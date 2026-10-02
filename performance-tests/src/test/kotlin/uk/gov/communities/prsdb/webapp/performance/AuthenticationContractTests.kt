package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AuthenticationContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `login preserves authorize query hidden fields form action and session cookie`() {
        AuthenticationServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(
                listOf(
                    "/oauth2/authorization/one-login",
                    "/authorize?state=state%2Bvalue&nonce=nonce-value",
                    "/form-submit?flow=login",
                    "/login/oauth2/code/one-login?code=code-value&state=state%2Bvalue",
                    "/landlord/landlord-details",
                    "/mutation",
                ),
                server.paths.toList(),
            )
            assertEquals("state+value", server.submittedFields["state"])
            assertEquals("encoded + & request", server.submittedFields["authRequestParams"])
            assertEquals("new hidden + & value", server.submittedFields["extraHidden"])
            assertEquals("synthetic-landlord", server.submittedFields["sub"])
            assertEquals("continue", server.submittedFields["continue"])
            assertEquals("""[{"code":"0"}]""", server.submittedFields["returnCodes"])
            assertEquals("", server.submittedFields["passportDetails"])
        }
    }

    @Test
    fun `relative simulator redirects and actions retain the rotated authenticated cookie`() {
        AuthenticationServer(relativeUrls = true).use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals("/mutation", server.paths.last())
            assertEquals("synthetic-landlord", server.submittedFields["sub"])
        }
    }

    @Test
    fun `missing required authorization field fails before simulator submission`() {
        AuthenticationServer(includeAuthCode = false).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.paths.any { it.startsWith("/form-submit") || it == "/mutation" })
        }
    }

    @Test
    fun `off-origin simulator form fails without sending identity`() {
        AuthenticationServer(formAction = "http://127.0.0.1:1/form-submit").use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.paths.any { it.startsWith("/form-submit") || it == "/mutation" })
        }
    }

    @Test
    fun `failed callback prevents subsequent journey mutations`() {
        AuthenticationServer(callbackStatus = 401).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.paths.contains("/mutation"))
        }
    }

    @Test
    fun `login page returned with status 200 is not accepted as authenticated details`() {
        AuthenticationServer(authenticated = false).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.paths.contains("/mutation"))
        }
    }

    private fun runSimulation(server: AuthenticationServer): GatlingTestRunner.Result =
        GatlingTestRunner(outputDirectory).run(
            AuthenticationContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-landlord",
            ),
        )

    private class AuthenticationServer(
        private val includeAuthCode: Boolean = true,
        private val formAction: String? = null,
        private val callbackStatus: Int = 302,
        private val authenticated: Boolean = true,
        private val relativeUrls: Boolean = false,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl: String
            get() = "http://127.0.0.1:${server.address.port}"
        val paths = ConcurrentLinkedQueue<String>()
        var submittedFields: Map<String, String> = emptyMap()
            private set

        init {
            server.createContext("/") { exchange ->
                paths.add(exchange.requestURI.toString())
                val cookie = exchange.requestHeaders.getFirst("Cookie").orEmpty()
                val prefix = if (relativeUrls) "" else baseUrl
                val (status, body) =
                    when (exchange.requestURI.path) {
                        "/oauth2/authorization/one-login" -> {
                            exchange.responseHeaders.add("Set-Cookie", "SESSION=auth-session; Path=/; HttpOnly")
                            exchange.responseHeaders.add("Location", "$prefix/authorize?state=state%2Bvalue&nonce=nonce-value")
                            302 to ""
                        }
                        "/authorize" -> {
                            val authCode =
                                if (includeAuthCode) """<input type="hidden" name="authCode" value="code-value">""" else ""
                            200 to """
                                <form method="post" action="${formAction ?: "$prefix/form-submit?flow=login"}">
                                  $authCode
                                  <input type="hidden" name="authRequestParams" value="encoded + &amp; request">
                                  <input type="hidden" name="state" value="state+value">
                                  <input type="hidden" name="extraHidden" value="new hidden + &amp; value">
                                  <input name="sub" value="default-subject">
                                  <input name="email" value="test@example.com">
                                  <input name="phoneNumber" value="07123456789">
                                  <input name="maxLoCAchieved" value="P2">
                                  <textarea name="returnCodes">[{"code":"0"}]</textarea>
                                  <textarea name="passportDetails"></textarea>
                                  <button name="continue" value="continue">Continue</button>
                                </form>
                                """.trimIndent()
                        }
                        "/form-submit" -> {
                            submittedFields =
                                exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                                    val parts = it.split("=", limit = 2)
                                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                        URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                                }
                            if (cookie.contains("SESSION=auth-session") && submittedFields["authCode"] == "code-value") {
                                exchange.responseHeaders.add(
                                    "Location",
                                    "$prefix/login/oauth2/code/one-login?code=code-value&state=state%2Bvalue",
                                )
                                302 to ""
                            } else {
                                403 to "Invalid authentication submission"
                            }
                        }
                        "/login/oauth2/code/one-login" -> {
                            exchange.responseHeaders.add("Location", "/")
                            exchange.responseHeaders.add("Set-Cookie", "SESSION=authenticated-session; Path=/; HttpOnly")
                            if (cookie.contains("SESSION=auth-session")) callbackStatus to "" else 403 to "Missing session"
                        }
                        "/landlord/landlord-details" ->
                            if (!cookie.contains("SESSION=authenticated-session")) {
                                401 to "Missing authenticated session"
                            } else {
                                200 to if (authenticated) {
                                    """<a href="/landlord/landlord-details/update-phone-number">Change phone number</a>"""
                                } else {
                                    "<h1>Sign in</h1>"
                                }
                            }
                        "/mutation" ->
                            if (cookie.contains("SESSION=authenticated-session")) {
                                200 to "Mutation performed"
                            } else {
                                401 to "Missing authenticated session"
                            }
                        else -> 404 to "Not found"
                    }
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1 else bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }

        override fun close() {
            server.stop(0)
        }
    }
}
