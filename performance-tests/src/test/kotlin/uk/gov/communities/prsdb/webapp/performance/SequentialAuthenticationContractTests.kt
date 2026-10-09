package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SequentialAuthenticationContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `sequential authentication populations use distinct subjects and isolated app and simulator cookies`() {
        SequentialAuthenticationServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(listOf("seeded-phone-subject", "fresh-registration-subject"), server.submittedSubjects.toList())
            assertEquals(listOf("", ""), server.appStartCookies.toList())
            assertEquals(listOf("", ""), server.simulatorAuthorizeCookies.toList())
            assertTrue(server.callbackCookies.toList()[0].orEmpty().contains("APP_SESSION=app-session-1"))
            assertTrue(server.callbackCookies.toList()[1].orEmpty().contains("APP_SESSION=app-session-2"))
            assertTrue(server.simulatorSubmitCookies.toList()[0].orEmpty().contains("SIM_SESSION=sim-session-1"))
            assertTrue(server.simulatorSubmitCookies.toList()[1].orEmpty().contains("SIM_SESSION=sim-session-2"))
        }
    }

    private fun runSimulation(server: SequentialAuthenticationServer): GatlingTestRunner.Result =
        GatlingTestRunner(outputDirectory).run(
            SequentialAuthenticationContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.baseUrl" to server.appUrl,
                "gatling.simulatorUrl" to server.simulatorUrl,
                "gatling.basic.landlordSubject" to "seeded-phone-subject",
                "gatling.basic.registrationSubject" to "fresh-registration-subject",
            ),
        )

    private class SequentialAuthenticationServer : AutoCloseable {
        private val appServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        private val simulatorServer = HttpServer.create(InetSocketAddress("localhost", 0), 0)
        val appUrl: String
            get() = "http://127.0.0.1:${appServer.address.port}"
        val simulatorUrl: String
            get() = "http://localhost:${simulatorServer.address.port}"
        private val flowCount = AtomicInteger()
        val appStartCookies = ConcurrentLinkedQueue<String>()
        val simulatorAuthorizeCookies = ConcurrentLinkedQueue<String>()
        val simulatorSubmitCookies = ConcurrentLinkedQueue<String>()
        val callbackCookies = ConcurrentLinkedQueue<String>()
        val submittedSubjects = ConcurrentLinkedQueue<String>()

        init {
            appServer.createContext("/") { exchange ->
                val (status, body) =
                    when (exchange.requestURI.path) {
                        "/oauth2/authorization/one-login" -> {
                            val flow = flowCount.incrementAndGet()
                            appStartCookies.add(exchange.requestHeaders.getFirst("Cookie").orEmpty())
                            exchange.responseHeaders.add("Set-Cookie", "APP_SESSION=app-session-$flow; Path=/; HttpOnly")
                            exchange.responseHeaders.add(
                                "Location",
                                "$simulatorUrl/authorize?flow=$flow",
                            )
                            302 to ""
                        }
                        "/login/oauth2/code/one-login" -> {
                            callbackCookies.add(exchange.requestHeaders.getFirst("Cookie").orEmpty())
                            302 to ""
                        }
                        else -> 404 to "Not found"
                    }
                respond(exchange, status, body, "text/html")
            }
            simulatorServer.createContext("/") { exchange ->
                val flow = queryParameter(exchange.requestURI.rawQuery.orEmpty(), "flow")
                val (status, body) =
                    when (exchange.requestURI.path) {
                        "/authorize" -> {
                            simulatorAuthorizeCookies.add(exchange.requestHeaders.getFirst("Cookie").orEmpty())
                            exchange.responseHeaders.add("Set-Cookie", "SIM_SESSION=sim-session-$flow; Path=/; HttpOnly")
                            200 to
                                """
                                <form method="post" action="$simulatorUrl/form-submit?flow=$flow">
                                  <input type="hidden" name="authCode" value="code-$flow">
                                  <input type="hidden" name="authRequestParams" value="request-$flow">
                                  <input type="hidden" name="state" value="state-$flow">
                                  <input name="sub" value="default-subject">
                                  <input name="email" value="synthetic@example.com">
                                  <input name="phoneNumber" value="02079460123">
                                  <input name="maxLoCAchieved" value="P2">
                                  <textarea name="returnCodes">[{"code":"00"}]</textarea>
                                  <textarea name="passportDetails"></textarea>
                                  <button name="continue" value="continue">Continue</button>
                                </form>
                                """.trimIndent()
                        }
                        "/form-submit" -> {
                            simulatorSubmitCookies.add(exchange.requestHeaders.getFirst("Cookie").orEmpty())
                            val submitted = parseForm(exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8))
                            submittedSubjects.add(submitted["sub"].orEmpty())
                            exchange.responseHeaders.add(
                                "Location",
                                "$appUrl/login/oauth2/code/one-login?code=${URLEncoder.encode(
                                    submitted["sub"],
                                    StandardCharsets.UTF_8,
                                )}&state=state-$flow",
                            )
                            302 to ""
                        }
                        else -> 404 to "Not found"
                    }
                respond(exchange, status, body, "text/html")
            }
            appServer.start()
            simulatorServer.start()
        }

        override fun close() {
            appServer.stop(0)
            simulatorServer.stop(0)
        }

        private fun queryParameter(
            query: String,
            name: String,
        ): String =
            query.split("&")
                .map { it.split("=", limit = 2) }
                .first { it[0] == name }
                .let { URLDecoder.decode(it[1], StandardCharsets.UTF_8) }

        private fun parseForm(body: String): Map<String, String> =
            body.split("&").associate {
                val parts = it.split("=", limit = 2)
                URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            }

        private fun respond(
            exchange: HttpExchange,
            status: Int,
            body: String,
            contentType: String,
        ) {
            val bytes = body.toByteArray(StandardCharsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "$contentType; charset=utf-8")
            exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1 else bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
    }
}
