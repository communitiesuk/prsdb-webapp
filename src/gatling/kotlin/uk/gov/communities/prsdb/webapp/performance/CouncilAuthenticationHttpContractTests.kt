package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals

class CouncilAuthenticationHttpContractTests {
    @Test
    fun `internal access preserves simulator fields state cookie and database subject`() {
        AuthenticationServer().use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
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

    private fun run(server: AuthenticationServer): GatlingTestRunner.Result {
        val directory =
            Files.createDirectories(
                Path.of(
                    System.getProperty("user.home"),
                    ".copilot",
                    "session-state",
                    "0adcbe66-1747-4ffa-ba2b-ff54b2db6dfe",
                    "files",
                    "council-auth-contract-${UUID.randomUUID()}",
                ),
            )
        return GatlingTestRunner(directory).run(
            CouncilContractSimulation::class.java,
            mapOf(
                "gatling.contract.council" to "authentication",
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.basic.iterations" to "1",
                "gatling.basic.landlordSubject" to "council-db-subject",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to "http://localhost:3000",
                "gatling.council.simulatorUrl" to server.baseUrl,
            ),
        )
    }

    private class AuthenticationServer(private val wrongCallback: Boolean = false) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val callbacks = AtomicInteger()

        init {
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
                            <textarea name="custom">retained</textarea></form>"""
                    }
                    "/submit" -> {
                        val fields =
                            exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                                val pair = it.split("=", limit = 2)
                                URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                    URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                            }
                        if (fields["sub"] != "council-db-subject" || fields["state"] != "correlated-state" ||
                            fields["authCode"] != "correlated-code" || fields["authRequestParams"] != "correlated-request" ||
                            fields["custom"] != "retained" || fields["version"] != "current" ||
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
                            exchange.responseHeaders.add("Location", "/local-council/dashboard")
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

        override fun close() = server.stop(0)
    }
}
