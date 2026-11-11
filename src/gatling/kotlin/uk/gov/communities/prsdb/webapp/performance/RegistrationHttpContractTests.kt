package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegistrationHttpContractTests {
    @Test
    fun `completes real correlated tasks and hosted payment and reloads persisted property`() {
        RegistrationServer().use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertTrue(server.posts.all { it["_csrf"] == "registration-csrf" && it["version"] == "registration-version" })
            assertTrue(server.posts.any { it["exemptionReason"] == "ANNUAL_USE_LESS_THAN_4_MONTHS" })
            assertEquals(1, server.cardVisits)
            assertEquals(2, server.persistedReads)
        }
    }

    @Test
    fun `missing CSRF cookie or changed journey action stops before mutation`() {
        for (defect in listOf("csrf", "action", "redirect", "cookie")) {
            RegistrationServer(defect).use { server ->
                val result = run(server)
                assertEquals(2, result.exitCode, "$defect: ${result.output}")
                assertTrue(server.posts.isEmpty(), "$defect mutated registration")
            }
        }
    }

    @Test
    fun `successful statuses cannot hide an unsaved property or missing compliance`() {
        for (defect in listOf("property", "compliance")) {
            RegistrationServer(defect).use { server ->
                val result = run(server)
                assertEquals(2, result.exitCode, "$defect: ${result.output}")
                assertEquals(1, server.cardVisits)
            }
        }
    }

    @Test
    fun `a failed payment stops the happy path without retrying`() {
        RegistrationServer("payment-failed").use { server ->
            val result = run(server)
            assertEquals(2, result.exitCode, result.output)
            assertEquals(1, server.cardVisits)
            assertEquals(0, server.persistedReads)
        }
    }

    @Test
    fun `pending payment remains unregistered until status polling completes`() {
        RegistrationServer("pending").use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.cardVisits)
            assertEquals(2, server.pendingResponses)
            assertEquals(2, server.pendingPropertyChecks)
        }
    }

    private fun run(server: RegistrationServer): GatlingTestRunner.Result {
        val directory = Files.createDirectories(Path.of("build/registration-contract-tests", UUID.randomUUID().toString()))
        return GatlingTestRunner(directory).run(
            RegistrationContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-landlord",
                "gatling.basic.mode" to "baseline",
                "gatling.basic.iterations" to "1",
            ),
        )
    }

    private class RegistrationServer(private val defect: String? = null) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val posts = ConcurrentLinkedQueue<Map<String, String>>()
        var cardVisits = 0
        var persistedReads = 0
        var pendingResponses = 0
        var pendingPropertyChecks = 0
        private var registered = false
        private val route = RegistrationDestination.ROUTE
        private val tasks = listOf("lookup-address", "has-electrical-safety", "start", "check-answers")
        private val forms =
            linkedMapOf(
                "lookup-address" to listOf("postcode", "houseNameOrNumber"),
                "select-address" to listOf("address"), "manual-address" to listOf("addressLineOne", "townOrCity", "postcode"),
                "local-council" to listOf("localCouncilId"), "property-type" to listOf("propertyType"),
                "number-of-bedrooms" to listOf("numberOfBedrooms"), "ownership-type" to listOf("ownershipType"),
                "has-joint-landlords" to listOf("hasJointLandlords"), "correspondence-email" to listOf("correspondenceEmailOption"),
                "correspondence-address/lookup-address" to listOf("postcode", "houseNameOrNumber"),
                "correspondence-address/select-address" to listOf("address"),
                "correspondence-address/manual-address" to listOf("addressLineOne", "townOrCity", "postcode"),
                "occupancy" to listOf("occupied"), "licensing-type" to listOf("licensingType"), "has-gas-supply" to listOf("hasGasSupply"),
                "check-gas-safety-answers" to emptyList(), "has-electrical-safety" to listOf("electricalCertType"),
                "electrical-safety-certificate-missing" to emptyList(), "check-electrical-safety-answers" to emptyList(),
                "has-epc" to listOf("hasCert"), "is-epc-required" to listOf("epcRequired"), "epc-exemption" to listOf("exemptionReason"),
                "check-epc-answers" to emptyList(), "check-answers" to emptyList(), "payment-summary" to emptyList(),
            )

        init {
            server.createContext("/") { exchange ->
                val uri = exchange.requestURI
                val path = uri.path
                val current = path.removePrefix("$route/")
                var code = 200
                var body = ""

                fun redirect(destination: String) {
                    code = 302
                    exchange.responseHeaders.add("Location", destination)
                }

                fun location(step: String) = "$route/$step?journeyId=journey-a"

                fun cookiePresent() = exchange.requestHeaders.getFirst("Cookie").orEmpty().contains("REGISTRATION=actor-a")
                if (path == "$route/task-list" && uri.rawQuery == null) {
                    if (defect != "cookie") exchange.responseHeaders.add("Set-Cookie", "REGISTRATION=actor-a; Path=/; HttpOnly")
                    redirect(
                        location(
                            "task-list",
                        ).replace("journey-a", if (defect == "redirect") "journey-b&journeyId=journey-a" else "journey-a"),
                    )
                } else if (!cookiePresent()) {
                    code = 403
                } else if (exchange.requestMethod == "POST") {
                    val fields =
                        exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split('&').associate { value ->
                            val pair = value.split('=', limit = 2)
                            URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                    posts.add(fields)
                    if (fields["_csrf"] != "registration-csrf" || fields["version"] != "registration-version") {
                        code = 403
                    } else if (current == "payment-return" && defect == "pending") {
                        registered = pendingResponses >= 2
                        redirect(location("payment-return"))
                    } else if (current == "payment-summary") {
                        redirect("/local/gov-uk-pay/mock-card-page/payment-one")
                    } else if (current in listOf("check-gas-safety-answers", "check-electrical-safety-answers", "check-epc-answers")) {
                        redirect(location("task-list"))
                    } else {
                        redirect(location(forms.keys.toList()[forms.keys.indexOf(current) + 1]))
                    }
                } else if (path == "/local/gov-uk-pay/mock-card-page/payment-one") {
                    cardVisits++
                    registered = defect !in listOf("payment-failed", "pending")
                    redirect(location("payment-return") + "&paymentReference=payment-reference")
                } else if (current == "payment-return" && defect == "pending" && !registered) {
                    pendingResponses++
                    body = """<div id="payment-status-no-js-content">Payment processing</div>""" +
                        """<form method="post" action="${location("payment-return")}">""" +
                        """<input type="hidden" name="_csrf" value="registration-csrf">""" +
                        """<input type="hidden" name="version" value="registration-version"></form>"""
                } else if (current == "payment-return") {
                    redirect(if (registered) "$route/confirmation" else location("payment-failed"))
                } else if (current == "payment-failed") {
                    body = """<h1>Payment failed</h1><a href="${location("payment-summary")}">Try again</a>"""
                } else if (current == "task-list") {
                    body = tasks.joinToString("") { """<a class="govuk-task-list__link" href="${location(it)}">$it</a>""" }
                } else if (current == "start") {
                    redirect(location("has-epc"))
                } else if (current == "confirmation" && registered) {
                    body = """<main><div class="govuk-panel"><h1>Registration complete</h1>""" +
                        """NFT registration 901, Performance Town, FA1 1AA</div><strong>P-1234-5678</strong></main>"""
                } else if (path == "/landlord/landlord-details") {
                    if (defect == "pending" && !registered) pendingPropertyChecks++
                    body =
                        if (registered && defect != "property") {
                            """<table><tr><td><a href="/landlord/property-details/100?withBackUrl=17">""" +
                                """NFT registration 901, Performance Town, FA1 1AA</a></td></tr></table>"""
                        } else {
                            "No properties"
                        }
                } else if (path == "/landlord/property-details/100") {
                    persistedReads++
                    body = """<h1>NFT registration 901</h1>""" +
                        """<div class="govuk-summary-list__row">Number of bedrooms 3</div>""" +
                        """<div class="govuk-summary-list__row">Licensing type None</div>""" +
                        """<div class="govuk-summary-list__row">Contact address NFT correspondence 901</div>""" +
                        """<div class="govuk-summary-list__row">Is this property occupied by tenants? No</div>""" +
                        if (defect == "compliance") {
                            ""
                        } else {
                            """<div class="govuk-summary-card">Gas safety certificate """ +
                                """Does the property have a gas supply or any gas appliances? No</div>""" +
                                """<div class="govuk-summary-card">Electrical safety certificate Provide this later</div>""" +
                                """<div class="govuk-summary-card">Energy performance certificate (EPC) less than 4 months a year</div>"""
                        }
                } else if (current in forms) {
                    val action = location(current).replace("journey-a", if (defect == "action") "wrong-journey" else "journey-a")
                    body = """<form method="post" action="$action">""" +
                        (if (defect == "csrf") "" else """<input type="hidden" name="_csrf" value="registration-csrf">""") +
                        """<input type="hidden" name="version" value="registration-version">""" +
                        forms.getValue(current).joinToString("") { """<input name="$it">""" } + "</form>"
                } else {
                    code = 404
                }
                exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                val bytes = body.toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(code, bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
                exchange.close()
            }
            server.start()
        }

        override fun close() {
            server.stop(0)
        }
    }
}
