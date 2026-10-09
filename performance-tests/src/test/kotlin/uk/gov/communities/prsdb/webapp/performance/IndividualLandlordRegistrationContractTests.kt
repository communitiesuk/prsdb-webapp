package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class IndividualLandlordRegistrationContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `individual registration submits every form with current csrf and journey id and reaches dashboard`() {
        RegistrationServer().use { server ->
            val result = runSimulation(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(
                listOf(
                    "/landlord/register-as-a-landlord/start",
                    "/landlord/register-as-a-landlord/privacy-notice",
                    "/landlord/register-as-a-landlord/privacy-notice?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/privacy-notice?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/verify-identity?journeyId=synthetic",
                    "/id-verification/oauth2/authorize/one-login",
                    "/authorize",
                    "/form-submit",
                    "/login/oauth2/code/one-login?code=identity-code&state=identity-state",
                    "/landlord/register-as-a-landlord/verify-identity?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/confirm-identity?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/confirm-identity?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/email?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/email?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/phone-number?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/phone-number?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/landlord-type?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/landlord-type?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/country-of-residence?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/country-of-residence?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/lookup-address?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/lookup-address?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/select-address?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/select-address?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/manual-address?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/manual-address?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/check-answers?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/check-answers?journeyId=synthetic",
                    "/landlord/register-as-a-landlord/confirmation",
                    "/oauth2/authorization/one-login",
                    "/authorize",
                    "/form-submit",
                    "/login/oauth2/code/one-login?code=identity-code&state=refresh-state",
                    "/landlord/register-as-a-landlord/confirmation",
                    "/landlord/dashboard",
                ),
                server.requests.map { if (it.path.startsWith("/authorize?")) "/authorize" else it.path },
            )
            assertEquals("true", server.submittedForms["privacy-notice"]?.get("agreesToPrivacyNotice"))
            assertEquals("synthetic-registration@example.com", server.submittedForms["email"]?.get("emailAddress"))
            assertEquals("02079460123", server.submittedForms["phone-number"]?.get("phoneNumber"))
            assertEquals("INDIVIDUAL", server.submittedForms["landlord-type"]?.get("landlordType"))
            assertEquals("true", server.submittedForms["country-of-residence"]?.get("livesInEnglandOrWales"))
            assertEquals("EG1 2AA", server.submittedForms["lookup-address"]?.get("postcode"))
            assertEquals("MANUAL", server.submittedForms["select-address"]?.get("address"))
            assertEquals("1 Example Road", server.submittedForms["manual-address"]?.get("addressLineOne"))
            assertEquals("Townville", server.submittedForms["manual-address"]?.get("townOrCity"))
            assertEquals("SW1A 1AA", server.submittedForms["manual-address"]?.get("postcode"))
            assertEquals("snapshot-synthetic", server.submittedForms["check-answers"]?.get("submittedFilteredJourneyData"))
            assertTrue(server.requests.last().path == "/landlord/dashboard")
            assertTrue(server.registrationNumberWasRendered)
            assertTrue(server.submittedForms.values.all { it["_csrf"]?.startsWith("csrf-") == true })
            assertEquals(
                "synthetic-registration-subject",
                server.requests.single { it.path == "/form-submit" && it.fields["state"] == "identity-state" }.fields["sub"],
            )
            assertTrue(
                server.requests
                    .single { it.path == "/form-submit" && it.fields["state"] == "identity-state" }
                    .fields["coreIdentity"]
                    .orEmpty()
                    .contains("ALEXANDER"),
            )
        }
    }

    @Test
    fun `registration preflight stops before posting privacy consent for an already registered subject`() {
        RegistrationServer(alreadyRegistered = true).use { server ->
            val result = runSimulation(server)

            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.submittedForms.isNotEmpty())
            assertEquals(
                listOf(
                    "/landlord/register-as-a-landlord/start",
                    "/landlord/register-as-a-landlord/privacy-notice",
                    "/landlord/register-as-a-landlord/privacy-notice?journeyId=synthetic",
                ),
                server.requests.map { it.path },
            )
        }
    }

    private fun runSimulation(server: RegistrationServer): GatlingTestRunner.Result =
        GatlingTestRunner(outputDirectory).run(
            IndividualLandlordRegistrationContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-phone-subject",
                "gatling.basic.registrationSubject" to "synthetic-registration-subject",
            ),
        )

    private class RegistrationServer(
        private val alreadyRegistered: Boolean = false,
    ) : AutoCloseable {
        data class Request(
            val path: String,
            val cookie: String?,
            val fields: Map<String, String>,
        )

        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl: String
            get() = "http://127.0.0.1:${server.address.port}"
        val requests = ConcurrentLinkedQueue<Request>()
        val submittedForms = ConcurrentHashMap<String, Map<String, String>>()

        @Volatile
        var registrationNumberWasRendered = false
            private set

        @Volatile
        private var identityCallbackCompleted = false

        @Volatile
        private var reauthenticationPending = false

        @Volatile
        private var reauthenticationCompleted = false

        init {
            server.createContext("/") { exchange ->
                val path = exchange.requestURI.path
                val cookie = exchange.requestHeaders.getFirst("Cookie")
                val fields =
                    if (exchange.requestMethod == "POST") {
                        parseForm(exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8))
                    } else {
                        emptyMap()
                    }
                requests.add(Request(exchange.requestURI.toString(), cookie, fields))
                val (status, body, contentType) =
                    when {
                        exchange.requestMethod == "GET" && path == "/landlord/register-as-a-landlord/start" ->
                            Triple(
                                200,
                                """<a class="govuk-button" href="/landlord/register-as-a-landlord/privacy-notice">Start now</a>""",
                                "text/html",
                            )
                        exchange.requestMethod == "GET" &&
                            path == "/landlord/register-as-a-landlord/privacy-notice" &&
                            exchange.requestURI.rawQuery == null ->
                            redirect("/landlord/register-as-a-landlord/privacy-notice?journeyId=synthetic")
                        exchange.requestMethod == "GET" && path == "/landlord/register-as-a-landlord/privacy-notice" ->
                            if (alreadyRegistered) {
                                exchange.responseHeaders.add("Location", "/landlord/dashboard")
                                Triple(302, "", "text/html")
                            } else {
                                formResponse(exchange, "privacy-notice", "agreesToPrivacyNotice")
                            }
                        exchange.requestMethod == "GET" && path == "/landlord/register-as-a-landlord/verify-identity" ->
                            if (identityCallbackCompleted) {
                                redirect("/landlord/register-as-a-landlord/confirm-identity?journeyId=synthetic")
                            } else {
                                redirect("/id-verification/oauth2/authorize/one-login")
                            }
                        exchange.requestMethod == "GET" && path == "/oauth2/authorization/one-login" -> {
                            reauthenticationPending = true
                            redirect("$baseUrl/authorize?flow=refresh")
                        }
                        exchange.requestMethod == "GET" && path == "/id-verification/oauth2/authorize/one-login" -> {
                            exchange.responseHeaders.add("Set-Cookie", "IDV_STATE=identity-flow; Path=/; HttpOnly")
                            redirect("$baseUrl/authorize?vtr=%5B%22Cl.Cm.P2%22%5D&claims=$ENCODED_CLAIMS")
                        }
                        exchange.requestMethod == "GET" && path == "/authorize" ->
                            Triple(
                                200,
                                """
                                <form method="post" action="$baseUrl/form-submit">
                                  <input type="hidden" name="authCode" value="identity-code">
                                  <input type="hidden" name="authRequestParams" value="encoded-request">
                                  <input type="hidden" name="state" value="${if (reauthenticationPending) "refresh-state" else "identity-state"}">
                                  <input type="hidden" name="email" value="registration@example.com">
                                  <input type="hidden" name="emailVerified" value="true">
                                  <input type="hidden" name="phoneNumber" value="07123456789">
                                  <input type="hidden" name="phoneNumberVerified" value="true">
                                  <input type="hidden" name="maxLoCAchieved" value="P2">
                                  <input data-testid="sub" name="sub" value="default-subject">
                                  <textarea data-testid="core-identity-vc" name="coreIdentity"></textarea>
                                  <textarea data-testid="postal-address-details" name="address"></textarea>
                                  <textarea data-testid="return-codes" name="returnCodes"></textarea>
                                  <button name="continue" value="continue">Continue</button>
                                </form>
                                """.trimIndent(),
                                "text/html",
                            )
                        exchange.requestMethod == "POST" && path == "/form-submit" -> {
                            if (cookie?.contains("IDV_STATE=identity-flow") != true && !reauthenticationPending) {
                                Triple(403, "Missing identity session", "text/plain")
                            } else {
                                exchange.responseHeaders.add(
                                    "Location",
                                    "$baseUrl/login/oauth2/code/one-login?code=identity-code&state=${fields["state"]}",
                                )
                                Triple(302, "", "text/plain")
                            }
                        }
                        exchange.requestMethod == "GET" && path == "/login/oauth2/code/one-login" -> {
                            if (exchange.requestURI.rawQuery?.contains("state=refresh-state") == true) {
                                reauthenticationPending = false
                                reauthenticationCompleted = true
                                redirect("/landlord/register-as-a-landlord/confirmation")
                            } else {
                                identityCallbackCompleted = true
                                redirect("/landlord/register-as-a-landlord/verify-identity?journeyId=synthetic")
                            }
                        }
                        exchange.requestMethod == "GET" && formStep(path) != null ->
                            formResponse(exchange, requireNotNull(formStep(path)), requiredField(path))
                        exchange.requestMethod == "POST" && formStep(path) != null -> {
                            val step = requireNotNull(formStep(path))
                            val expectedToken = "csrf-$step"
                            if (
                                fields["_csrf"] != expectedToken ||
                                exchange.requestURI.rawQuery != "journeyId=synthetic"
                            ) {
                                Triple(403, "Invalid CSRF token or lost journey id", "text/plain")
                            } else {
                                submittedForms[step] = fields
                                redirect(nextPath(step))
                            }
                        }
                        exchange.requestMethod == "GET" &&
                            path == "/landlord/register-as-a-landlord/confirmation" &&
                            !reauthenticationCompleted ->
                            redirect("/oauth2/authorization/one-login")
                        exchange.requestMethod == "GET" && path == "/landlord/register-as-a-landlord/confirmation" -> {
                            registrationNumberWasRendered = true
                            Triple(
                                200,
                                """
                                <div class="govuk-panel__body"><strong>LRN-SYNTHETIC-001</strong></div>
                                <a href="/landlord/dashboard">Go to dashboard</a>
                                """.trimIndent(),
                                "text/html",
                            )
                        }
                        exchange.requestMethod == "GET" && path == "/landlord/dashboard" ->
                            Triple(200, "<h1>Landlord dashboard</h1>Landlord registration number", "text/html")
                        else -> Triple(404, "Not found", "text/plain")
                    }
                respond(exchange, status, body, contentType)
            }
            server.start()
        }

        override fun close() {
            server.stop(0)
        }

        private fun formResponse(
            exchange: HttpExchange,
            step: String,
            requiredField: String,
        ): Triple<Int, String, String> {
            if (exchange.requestURI.path.endsWith("/privacy-notice")) {
                exchange.responseHeaders.add("Set-Cookie", "APP_SESSION=registration-session; Path=/; HttpOnly")
            }
            val hiddenAnswer =
                if (step == "check-answers") {
                    """<input type="hidden" name="submittedFilteredJourneyData" value="snapshot-synthetic">"""
                } else {
                    ""
                }
            val input =
                if (step == "confirm-identity" || step == "check-answers") {
                    ""
                } else {
                    """<input name="$requiredField" value="">"""
                }
            val body = """
                <form method="post" action="${exchange.requestURI.path}?journeyId=synthetic">
                  <input type="hidden" name="_csrf" value="csrf-$step">
                  $hiddenAnswer
                  $input
                </form>
                """
            return Triple(200, body, "text/html")
        }

        private fun formStep(path: String): String? =
            path.substringAfter("/landlord/register-as-a-landlord/", "").takeIf {
                it in
                    setOf(
                        "privacy-notice",
                        "confirm-identity",
                        "email",
                        "phone-number",
                        "landlord-type",
                        "country-of-residence",
                        "lookup-address",
                        "select-address",
                        "manual-address",
                        "check-answers",
                    )
            }

        private fun requiredField(path: String): String =
            when (formStep(path)) {
                "privacy-notice" -> "agreesToPrivacyNotice"
                "confirm-identity", "check-answers" -> "_csrf"
                "email" -> "emailAddress"
                "phone-number" -> "phoneNumber"
                "landlord-type" -> "landlordType"
                "country-of-residence" -> "livesInEnglandOrWales"
                "lookup-address" -> "postcode"
                "select-address" -> "address"
                "manual-address" -> "addressLineOne"
                else -> "_csrf"
            }

        private fun nextPath(step: String): String =
            when (step) {
                "privacy-notice" -> "/landlord/register-as-a-landlord/verify-identity?journeyId=synthetic"
                "confirm-identity" -> "/landlord/register-as-a-landlord/email?journeyId=synthetic"
                "email" -> "/landlord/register-as-a-landlord/phone-number?journeyId=synthetic"
                "phone-number" -> "/landlord/register-as-a-landlord/landlord-type?journeyId=synthetic"
                "landlord-type" -> "/landlord/register-as-a-landlord/country-of-residence?journeyId=synthetic"
                "country-of-residence" -> "/landlord/register-as-a-landlord/lookup-address?journeyId=synthetic"
                "lookup-address" -> "/landlord/register-as-a-landlord/select-address?journeyId=synthetic"
                "select-address" -> "/landlord/register-as-a-landlord/manual-address?journeyId=synthetic"
                "manual-address" -> "/landlord/register-as-a-landlord/check-answers?journeyId=synthetic"
                "check-answers" -> "/landlord/register-as-a-landlord/confirmation"
                else -> error("No next route configured for registration step $step")
            }

        private fun redirect(location: String): Triple<Int, String, String> {
            // The exchange response header is attached in the caller, where the exchange is in scope.
            return Triple(302, location, "text/plain")
        }

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

        companion object {
            private const val CORE_IDENTITY_CLAIM = "https://vocab.account.gov.uk/v1/coreIdentityJWT"
            private const val ADDRESS_CLAIM = "https://vocab.account.gov.uk/v1/address"
            private const val RETURN_CODE_CLAIM = "https://vocab.account.gov.uk/v1/returnCode"
            private val ENCODED_CLAIMS =
                java.net.URLEncoder
                    .encode(
                        """{"userinfo":{"$CORE_IDENTITY_CLAIM":null,"$ADDRESS_CLAIM":null,"$RETURN_CODE_CLAIM":null}}""",
                        StandardCharsets.UTF_8,
                    )
        }
    }
}
