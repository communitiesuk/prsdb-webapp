package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CouncilAuthenticationHttpContractTests {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `internal access preserves simulator fields state cookie and database subject`() {
        AuthenticationServer().use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.callbacks.get())
        }
    }

    @Test
    fun `internal access accepts the NFT simulator form without textareas`() {
        AuthenticationServer(withTextarea = false).use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.callbacks.get())
        }
    }

    @Test
    fun `internal access and callback use the council origin rather than the landlord origin`() {
        AuthenticationServer().use { server ->
            val result = run(server, server.landlordBaseUrl)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.callbacks.get())
            assertTrue(server.landlordRequests.isEmpty())
        }
    }

    @Test
    fun `internal access rejects a callback redirect that does not reach the council dashboard`() {
        AuthenticationServer(wrongSuccessLocation = true).use { server ->
            val result = run(server)

            assertTrue(result.exitCode != 0, result.output)
            assertEquals(1, server.callbacks.get())
        }
    }

    @Test
    fun `internal access refuses a callback for the One Login provider`() {
        AuthenticationServer(wrongCallback = true).use { server ->
            val result = run(server)
            assertEquals(2, result.exitCode, result.output)
            assertEquals(0, server.callbacks.get())
        }
    }

    private fun run(
        server: AuthenticationServer,
        landlordBaseUrl: String = server.baseUrl,
    ): GatlingTestRunner.Result =
        GatlingTestRunner(directory).run(
            CouncilReadContractSimulation::class.java,
            mapOf(
                "gatling.council.mode" to "authentication",
                "gatling.council.subject" to "council-db-subject",
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.basic.landlordSubject" to "contract-only",
                "gatling.baseUrl" to landlordBaseUrl,
                "gatling.council.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to "http://localhost:3000",
                "gatling.council.simulatorUrl" to server.baseUrl,
            ),
        )

    private class AuthenticationServer(
        private val wrongCallback: Boolean = false,
        private val wrongSuccessLocation: Boolean = false,
        private val withTextarea: Boolean = true,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        private val landlordServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val landlordBaseUrl = "http://127.0.0.1:${landlordServer.address.port}"
        val callbacks = AtomicInteger()
        val landlordRequests = ConcurrentLinkedQueue<String>()

        init {
            landlordServer.createContext("/") { exchange ->
                landlordRequests.add(exchange.requestURI.path)
                exchange.sendResponseHeaders(404, -1)
                exchange.close()
            }
            landlordServer.start()
            server.createContext("/") { exchange ->
                var code = 200
                var body = ""
                when (exchange.requestURI.path) {
                    "/oauth2/authorization/internal-access" -> {
                        code = 302
                        exchange.responseHeaders.add("Set-Cookie", "SESSION=council-auth; Path=/")
                        exchange.responseHeaders.add("Location", "/authorize")
                    }

                    "/authorize" -> {
                        body = """<form method="post" action="/submit">
                            <input type="hidden" name="state" value="correlated-state">
                            <input type="hidden" name="authCode" value="correlated-code">
                            <input type="hidden" name="authRequestParams" value="correlated-request">
                            <input type="hidden" name="version" value="current">
                            <input name="email" value="council@example.com">
                            <input name="phoneNumber" value="02079460123">
                            <input name="maxLoCAchieved" value="P0">
                            ${if (withTextarea) """<textarea name="custom">retained</textarea>""" else ""}</form>"""
                    }

                    "/submit" -> {
                        val fields = formFields(exchange.requestBody.readAllBytes())
                        if (fields["sub"] != "council-db-subject" || fields["state"] != "correlated-state" ||
                            fields["authCode"] != "correlated-code" || fields["authRequestParams"] != "correlated-request" ||
                            fields["custom"] != (if (withTextarea) "retained" else null) || fields["version"] != "current" ||
                            fields["email"] != "council@example.com" || fields["phoneNumber"] != "02079460123" ||
                            fields["maxLoCAchieved"] != "P0" || fields["emailVerified"] != "true" ||
                            fields["phoneNumberVerified"] != "true" || fields["continue"] != "continue" ||
                            exchange.requestHeaders.getFirst("Cookie") != "SESSION=council-auth"
                        ) {
                            code = 403
                        } else {
                            code = 302
                            exchange.responseHeaders.add(
                                "Location",
                                if (wrongCallback) {
                                    "/login/oauth2/code/one-login?code=correlated-code&state=correlated-state"
                                } else {
                                    "/local-council/login/oauth2/code/internal-access?code=correlated-code&state=correlated-state"
                                },
                            )
                        }
                    }

                    "/local-council/login/oauth2/code/internal-access" -> {
                        callbacks.incrementAndGet()
                        if (exchange.requestURI.rawQuery != "code=correlated-code&state=correlated-state" ||
                            exchange.requestHeaders.getFirst("Cookie") != "SESSION=council-auth"
                        ) {
                            code = 403
                        } else {
                            code = 302
                            exchange.responseHeaders.add(
                                "Location",
                                if (wrongSuccessLocation) "/oauth2/authorization/internal-access" else "/local-council/dashboard",
                            )
                        }
                    }

                    else -> code = 404
                }
                exchange.responseHeaders.add("Content-Type", "text/html")
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(code, if (bytes.isEmpty()) -1 else bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }

        override fun close() {
            server.stop(0)
            landlordServer.stop(0)
        }
    }

    companion object {
        fun formFields(body: ByteArray): Map<String, String> =
            body.toString(StandardCharsets.UTF_8).split("&").associate {
                val pair = it.split("=", limit = 2)
                URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            }
    }
}
