package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpExchange
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

class LandlordEmailUpdateContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `email update preserves journey id csrf cookie and verifies saved value`() {
        EmailServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(
                listOf("GET entry", "GET form", "POST form", "GET details"),
                server.requests.toList(),
            )
            assertEquals(LandlordEmailUpdateJourney.EMAIL_ADDRESS, server.submittedEmail)
        }
    }

    @Test
    fun `validation errors with status 200 prevent email submission`() {
        EmailServer(includeValidationError = true).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.requests.contains("POST form"))
        }
    }

    @Test
    fun `wrong content type prevents email submission`() {
        EmailServer(contentType = "application/json").use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.requests.contains("POST form"))
        }
    }

    @Test
    fun `successful redirect without persisted email fails the journey`() {
        EmailServer(persistEmail = false).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertEquals("GET details", server.requests.lastOrNull())
        }
    }

    private fun runSimulation(server: EmailServer): GatlingTestRunner.Result =
        GatlingTestRunner(outputDirectory).run(
            LandlordEmailUpdateContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-landlord",
            ),
        )

    private class EmailServer(
        private val includeValidationError: Boolean = false,
        private val contentType: String = "text/html; charset=utf-8",
        private val persistEmail: Boolean = true,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl: String
            get() = "http://127.0.0.1:${server.address.port}"
        val requests = ConcurrentLinkedQueue<String>()
        var submittedEmail: String? = null
            private set
        private val emailStep = "/landlord/landlord-details/update-email/email"
        private val journeyQuery = "journeyId=synthetic%2Bjourney"

        init {
            server.createContext("/") { exchange ->
                val (status, body, responseContentType) =
                    when {
                        exchange.requestURI.path == emailStep && exchange.requestURI.rawQuery == null -> {
                            requests.add("GET entry")
                            exchange.responseHeaders.add("Set-Cookie", "SESSION=email-session; Path=/; HttpOnly")
                            exchange.responseHeaders.add("Location", "$emailStep?$journeyQuery")
                            Triple(302, "", "text/plain")
                        }
                        exchange.requestURI.path == emailStep &&
                            exchange.requestMethod == "GET" &&
                            exchange.requestURI.rawQuery == journeyQuery -> {
                            requests.add("GET form")
                            val error =
                                if (includeValidationError) {
                                    """<div class="govuk-error-summary">Invalid email</div>"""
                                } else {
                                    ""
                                }
                            Triple(
                                200,
                                """$error<form method="post" action=""><input type="hidden" name="_csrf" value="email-csrf"><input name="emailAddress"></form>""",
                                contentType,
                            )
                        }
                        exchange.requestURI.path == emailStep && exchange.requestMethod == "POST" -> {
                            requests.add("POST form")
                            val fields =
                                exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                                    val parts = it.split("=", limit = 2)
                                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                        URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                                }
                            submittedEmail = fields["emailAddress"]
                            if (
                                fields["_csrf"] == "email-csrf" &&
                                exchange.requestURI.rawQuery == journeyQuery &&
                                exchange.requestHeaders.getFirst("Cookie").orEmpty().contains("SESSION=email-session")
                            ) {
                                exchange.responseHeaders.add("Location", "/landlord/landlord-details")
                                Triple(302, "", "text/plain")
                            } else {
                                Triple(403, "Invalid CSRF, journey id or session", "text/plain")
                            }
                        }
                        exchange.requestURI.path == "/landlord/landlord-details" -> {
                            requests.add("GET details")
                            val email = if (persistEmail) submittedEmail else "old@example.invalid"
                            Triple(
                                200,
                                """<dl class="govuk-summary-list"><div class="govuk-summary-list__row"><dt>Email address</dt><dd class="govuk-summary-list__value">$email</dd><dd><a href="/landlord/landlord-details/update-email/email">Change</a></dd></div></dl>""",
                                "text/html; charset=utf-8",
                            )
                        }
                        else -> Triple(404, "Not found", "text/plain")
                    }
                respond(exchange, status, body, responseContentType)
            }
            server.start()
        }

        override fun close() {
            server.stop(0)
        }

        private fun respond(
            exchange: HttpExchange,
            status: Int,
            body: String,
            contentType: String,
        ) {
            val responseBody = body.toByteArray(StandardCharsets.UTF_8)
            if (status == 302 && exchange.responseHeaders.getFirst("Location") == null) {
                exchange.responseHeaders.add("Location", body)
            }
            exchange.responseHeaders.add("Content-Type", "$contentType; charset=utf-8")
            exchange.sendResponseHeaders(status, if (responseBody.isEmpty() || status == 302) -1 else responseBody.size.toLong())
            if (responseBody.isNotEmpty() && status != 302) {
                exchange.responseBody.use { it.write(responseBody) }
            } else {
                exchange.close()
            }
        }
    }
}
