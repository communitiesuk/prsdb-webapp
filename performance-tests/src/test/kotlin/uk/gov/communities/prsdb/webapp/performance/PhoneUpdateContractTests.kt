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

class PhoneUpdateContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `phone update preserves empty form action journey id CSRF cookie and verifies saved value`() {
        PhoneServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(
                listOf(
                    "GET phone entry",
                    "GET phone form",
                    "POST phone form",
                    "GET details",
                    "GET email entry",
                    "GET email form",
                    "POST email form",
                    "GET details",
                ),
                server.requests.toList(),
            )
            assertEquals(LandlordPhoneUpdateJourney.PHONE_NUMBER, server.submittedPhone)
            assertEquals(LandlordEmailUpdateJourney.EMAIL_ADDRESS, server.submittedEmail)
        }
    }

    @Test
    fun `wrong role cannot start the phone journey`() {
        PhoneServer(entryStatus = 403).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertEquals(listOf("GET phone entry"), server.requests.toList())
        }
    }

    @Test
    fun `missing journey id fails without submitting the phone update`() {
        PhoneServer(journeyQuery = "missing=journey").use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.requests.contains("POST phone form"))
        }
    }

    @Test
    fun `stale CSRF is rejected and does not reach saved details`() {
        PhoneServer(csrfValue = "stale-csrf").use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertEquals("POST phone form", server.requests.last())
        }
    }

    @Test
    fun `lost session fails the submission`() {
        PhoneServer(issueCookie = false).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertEquals("POST phone form", server.requests.last())
        }
    }

    @Test
    fun `save returning the input page is not treated as completion`() {
        PhoneServer(submitStatus = 200).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertEquals("POST phone form", server.requests.last())
        }
    }

    @Test
    fun `validation error markup with status 200 prevents mutation`() {
        PhoneServer(includeValidationError = true).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.requests.contains("POST phone form"))
        }
    }

    @Test
    fun `HTML content in a non-HTML response is rejected before mutation`() {
        PhoneServer(contentType = "application/json").use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.requests.contains("POST phone form"))
        }
    }

    @Test
    fun `missing CSRF fails before submitting the phone update`() {
        PhoneServer(includeCsrf = false).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.requests.contains("POST phone form"))
        }
    }

    @Test
    fun `successful redirect without persisted phone fails the journey`() {
        PhoneServer(persistPhone = false).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertEquals("GET details", server.requests.last())
        }
    }

    private fun runSimulation(server: PhoneServer): GatlingTestRunner.Result =
        GatlingTestRunner(outputDirectory).run(
            PhoneUpdateContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-landlord",
            ),
        )

    @Suppress("ktlint:standard:max-line-length")
    private class PhoneServer(
        private val includeCsrf: Boolean = true,
        private val persistPhone: Boolean = true,
        private val entryStatus: Int = 302,
        private val journeyQuery: String = "journeyId=synthetic%2Bjourney",
        private val csrfValue: String = "phone-csrf",
        private val issueCookie: Boolean = true,
        private val submitStatus: Int = 302,
        private val contentType: String = "text/html; charset=utf-8",
        private val includeValidationError: Boolean = false,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl: String
            get() = "http://127.0.0.1:${server.address.port}"
        val requests = ConcurrentLinkedQueue<String>()
        var submittedPhone: String? = null
            private set
        var submittedEmail: String? = null
            private set
        private val step = "/landlord/landlord-details/update-phone-number/phone-number"
        private val emailStep = "/landlord/landlord-details/update-email/email"
        private val emailJourneyQuery = "journeyId=email-synthetic"
        private var detailsCount = 0

        init {
            server.createContext("/") { exchange ->
                val (status, body) =
                    when {
                        exchange.requestURI.path == step && exchange.requestURI.rawQuery == null -> {
                            requests.add("GET phone entry")
                            if (issueCookie) {
                                exchange.responseHeaders.add("Set-Cookie", "SESSION=phone-session; Path=/; HttpOnly")
                            }
                            exchange.responseHeaders.add("Location", "$step?$journeyQuery")
                            entryStatus to ""
                        }
                        exchange.requestURI.path == step && exchange.requestMethod == "GET" -> {
                            requests.add("GET phone form")
                            val csrf =
                                if (includeCsrf) """<input type="hidden" name="_csrf" value="$csrfValue">""" else ""
                            if (exchange.requestURI.rawQuery == "journeyId=synthetic%2Bjourney") {
                                val error =
                                    if (includeValidationError) """<div class="govuk-error-summary">Invalid phone</div>""" else ""
                                200 to """$error<form method="post" action="">$csrf<input name="phoneNumber"></form>"""
                            } else {
                                400 to "Missing journey id"
                            }
                        }
                        exchange.requestURI.path == step && exchange.requestMethod == "POST" -> {
                            requests.add("POST phone form")
                            val fields =
                                exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                                    val parts = it.split("=", limit = 2)
                                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                        URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                                }
                            submittedPhone = fields["phoneNumber"]
                            if (
                                fields["_csrf"] == "phone-csrf" &&
                                exchange.requestURI.rawQuery == "journeyId=synthetic%2Bjourney" &&
                                exchange.requestHeaders.getFirst("Cookie").orEmpty().contains("SESSION=phone-session")
                            ) {
                                exchange.responseHeaders.add("Location", "/landlord/landlord-details")
                                submitStatus to ""
                            } else {
                                403 to "Invalid journey submission"
                            }
                        }
                        exchange.requestURI.path == emailStep &&
                            exchange.requestMethod == "GET" &&
                            exchange.requestURI.rawQuery == null -> {
                            requests.add("GET email entry")
                            exchange.responseHeaders.add("Location", "$emailStep?$emailJourneyQuery")
                            302 to ""
                        }
                        exchange.requestURI.path == emailStep &&
                            exchange.requestMethod == "GET" &&
                            exchange.requestURI.rawQuery == emailJourneyQuery -> {
                            requests.add("GET email form")
                            200 to """<form method="post" action=""><input type="hidden" name="_csrf" value="email-csrf"><input name="emailAddress"></form>"""
                        }
                        exchange.requestURI.path == emailStep && exchange.requestMethod == "POST" -> {
                            requests.add("POST email form")
                            val fields =
                                exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").associate {
                                    val parts = it.split("=", limit = 2)
                                    URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                        URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                                }
                            submittedEmail = fields["emailAddress"]
                            if (
                                fields["_csrf"] == "email-csrf" &&
                                exchange.requestURI.rawQuery == emailJourneyQuery &&
                                exchange.requestHeaders.getFirst("Cookie").orEmpty().contains("SESSION=phone-session")
                            ) {
                                exchange.responseHeaders.add("Location", "/landlord/landlord-details")
                                302 to ""
                            } else {
                                403 to "Invalid email journey submission"
                            }
                        }
                        exchange.requestURI.path == "/landlord/landlord-details" -> {
                            requests.add("GET details")
                            detailsCount++
                            val saved = if (persistPhone) submittedPhone else "07123456789"
                            200 to
                                """
                                <div class="govuk-summary-list__row">
                                  <dd class="govuk-summary-list__value">$saved</dd>
                                  <dd><a href="$step">Change phone number</a></dd>
                                </div>
                                <div class="govuk-summary-list__row">
                                  <dd class="govuk-summary-list__value">${if (detailsCount > 1) submittedEmail else "old@example.invalid"}</dd>
                                  <dd><a href="$emailStep">Change email</a></dd>
                                </div>
                                """.trimIndent()
                        }
                        else -> 404 to "Not found"
                    }
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                exchange.responseHeaders.add("Content-Type", contentType)
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
