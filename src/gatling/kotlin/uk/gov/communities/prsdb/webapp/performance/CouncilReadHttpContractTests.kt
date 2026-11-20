package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CouncilReadHttpContractTests {
    @TempDir
    lateinit var directory: Path

    @Test
    fun `basic council read journey requests only dashboard LRN and PRN searches and their details`() {
        ReadServer().use { server ->
            val result = runBasic(server)

            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.callbacks.size)
            assertTrue(server.landlordRequests.isEmpty())
            assertEquals(
                listOf(
                    "/local-council/dashboard",
                    "/local-council/search/landlord",
                    "/local-council/landlord-details/300",
                    "/local-council/search/property",
                    "/local-council/property-details/100",
                ),
                server.paths.drop(4),
            )
            assertEquals(
                listOf(
                    RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 400).toString(),
                    RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 200).toString(),
                ),
                server.searchTerms.toList(),
            )
            assertTrue(server.licenseFilters.isEmpty())
            assertTrue(server.councilFilters.isEmpty())
            assertTrue(server.pages.isEmpty())
        }
    }

    @Test
    fun `basic council read journey fails before drilldown when expected property is absent`() {
        ReadServer(propertyPresent = false).use { server ->
            val result = runBasic(server)

            assertTrue(result.exitCode != 0, result.output)
            assertEquals("/local-council/search/property", server.paths.last())
        }
    }

    @Test
    fun `basic council read journey fails before following a wrong property destination`() {
        ReadServer(propertyDestination = "/local-council/property-details/1000").use { server ->
            val result = runBasic(server)

            assertTrue(result.exitCode != 0, result.output)
            assertEquals("/local-council/search/property", server.paths.last())
        }
    }

    @Test
    fun `council read journey covers dashboard searches filters pagination and record details`() {
        ReadServer().use { server ->
            val fixturePath = directory.resolve("council-fixture.json")
            Files.writeString(fixturePath, fixture)
            val result =
                GatlingTestRunner(directory).run(
                    CouncilReadContractSimulation::class.java,
                    mapOf(
                        "gatling.council.mode" to "read",
                        "gatling.council.fixturePath" to fixturePath.toString(),
                        "gatling.council.subject" to "council-db-subject",
                        "gatling.council.simulatorUrl" to server.baseUrl,
                        "gatling.target" to "local",
                        "gatling.basic.mode" to "baseline",
                        "gatling.basic.landlordSubject" to "contract-only",
                        "gatling.baseUrl" to server.landlordBaseUrl,
                        "gatling.council.baseUrl" to server.baseUrl,
                        "gatling.simulatorUrl" to "http://localhost:3000",
                    ),
                )

            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.callbacks.size)
            assertTrue(server.landlordRequests.isEmpty())
            assertTrue(
                server.paths.containsAll(
                    listOf(
                        "/local-council/dashboard",
                        "/local-council/search/landlord",
                        "/local-council/search/property",
                        "/local-council/landlord-details/300",
                        "/local-council/property-details/100",
                    ),
                ),
            )
            assertTrue(
                server.searchTerms.containsAll(
                    listOf(
                        "Fixture Landlord",
                        "100000",
                        "AB1",
                    ),
                ),
            )
            assertTrue(server.licenseFilters.contains("NO_LICENSING"))
            assertTrue(server.councilFilters.contains("true"))
            assertTrue(server.pages.contains("2"))
        }
    }

    private fun runBasic(server: ReadServer): GatlingTestRunner.Result {
        val fixturePath = directory.resolve("basic-council-fixture.json")
        Files.writeString(fixturePath, fixture)
        return GatlingTestRunner(directory).run(
            BasicCouncilReadContractSimulation::class.java,
            mapOf(
                "gatling.council.fixturePath" to fixturePath.toString(),
                "gatling.council.simulatorUrl" to server.baseUrl,
                "gatling.target" to "local",
                "gatling.basic.mode" to "baseline",
                "gatling.basic.landlordSubject" to "contract-only",
                "gatling.baseUrl" to server.landlordBaseUrl,
                "gatling.council.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to "http://localhost:3000",
            ),
        )
    }

    private class ReadServer(
        private val propertyPresent: Boolean = true,
        private val propertyDestination: String? = null,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        private val landlordServer = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val landlordBaseUrl = "http://127.0.0.1:${landlordServer.address.port}"
        val paths = ConcurrentLinkedQueue<String>()
        val landlordRequests = ConcurrentLinkedQueue<String>()
        val searchTerms = ConcurrentLinkedQueue<String>()
        val licenseFilters = ConcurrentLinkedQueue<String>()
        val councilFilters = ConcurrentLinkedQueue<String>()
        val pages = ConcurrentLinkedQueue<String>()
        val callbacks = ConcurrentLinkedQueue<String>()
        private val authenticated = AtomicBoolean(false)

        private val landlordRegistration =
            RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 400).toString()
        private val propertyRegistration =
            RegistrationNumberDataModel(RegistrationNumberType.PROPERTY, 200).toString()

        init {
            landlordServer.createContext("/") { exchange ->
                landlordRequests.add(exchange.requestURI.path)
                exchange.sendResponseHeaders(404, -1)
                exchange.close()
            }
            landlordServer.start()
            server.createContext("/") { exchange ->
                val path = exchange.requestURI.path
                paths.add(path)
                var code = 200
                var body = ""
                when (path) {
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
                            <input name="email" value="council@example.com">
                            <input name="phoneNumber" value="02079460123">
                            <input name="maxLoCAchieved" value="P0">
                            <textarea name="custom">retained</textarea></form>"""
                    }

                    "/submit" -> {
                        val fields = CouncilAuthenticationHttpContractTests.formFields(exchange.requestBody.readAllBytes())
                        if (fields["sub"] != "council-db-subject" || fields["state"] != "correlated-state") {
                            code = 403
                        } else {
                            code = 302
                            exchange.responseHeaders.add(
                                "Location",
                                "/local-council/login/oauth2/code/internal-access?code=correlated-code&state=correlated-state",
                            )
                        }
                    }

                    "/local-council/login/oauth2/code/internal-access" -> {
                        callbacks.add(exchange.requestURI.rawQuery.orEmpty())
                        authenticated.set(
                            exchange.requestURI.rawQuery == "code=correlated-code&state=correlated-state" &&
                                exchange.requestHeaders.getFirst("Cookie") == "SESSION=council-auth",
                        )
                        if (!authenticated.get()) {
                            code = 403
                        } else {
                            code = 302
                            exchange.responseHeaders.add("Location", "/local-council/dashboard")
                        }
                    }

                    "/local-council/dashboard" -> {
                        if (!isAuthenticated(exchange)) {
                            code = 401
                        } else {
                            body = """<main><h1>Fixture Council User</h1>
                                <a href="/local-council/search/landlord">Search landlords</a>
                                <a href="/local-council/search/property">Search properties</a></main>"""
                        }
                    }

                    "/local-council/search/landlord" -> {
                        if (!isAuthenticated(exchange)) {
                            code = 401
                        } else {
                            val term = query(exchange.requestURI.rawQuery)["searchTerm"].orEmpty()
                            query(exchange.requestURI.rawQuery)["restrictToLocalCouncil"]?.let(councilFilters::add)
                            searchTerms.add(term)
                            body =
                                """<main><table><tbody><tr><td><a href="/local-council/landlord-details/300">$landlordRegistration</a></td>
                                <td>$term</td></tr></tbody></table></main>"""
                        }
                    }

                    "/local-council/search/property" -> {
                        if (!isAuthenticated(exchange)) {
                            code = 401
                        } else {
                            val params = query(exchange.requestURI.rawQuery)
                            val term = params["searchTerm"].orEmpty()
                            searchTerms.add(term)
                            params["restrictToLocalCouncil"]?.let(councilFilters::add)
                            params["restrictToLicenses"]?.let(licenseFilters::add)
                            params["page"]?.let(pages::add)
                            val pageTwo = params["page"] == "2"
                            val propertyId = if (pageTwo) 101 else 100
                            val next =
                                if (!pageTwo && term == "AB1") {
                                    "<a rel=\"next\" href=\"/local-council/search/property?searchTerm=AB1&amp;page=2\">Next</a>"
                                } else {
                                    ""
                                }
                            val currentPage = if (pageTwo) "<a aria-current=\"page\">2</a>" else ""
                            val filters =
                                if (params.containsKey("restrictToLicenses")) {
                                    """<input name="restrictToLocalCouncil" checked>
                                    <input name="restrictToLicenses" value="${params["restrictToLicenses"]}" checked>"""
                                } else {
                                    ""
                                }
                            body =
                                if (propertyPresent) {
                                    """<main>$filters<table><tbody><tr><td><a href="${propertyDestination ?: "/local-council/property-details/$propertyId"}">$propertyRegistration</a></td>
                                    <td>$term</td></tr></tbody></table>$next$currentPage</main>"""
                                } else {
                                    "<main><table><tbody></tbody></table></main>"
                                }
                        }
                    }

                    "/local-council/landlord-details/300" -> {
                        if (!isAuthenticated(exchange)) {
                            code = 401
                        } else {
                            body =
                                """<main><h1>Fixture Landlord</h1><dl class="govuk-summary-list">""" +
                                """<dd>$landlordRegistration</dd></dl></main>"""
                        }
                    }

                    "/local-council/property-details/100", "/local-council/property-details/101" -> {
                        if (!isAuthenticated(exchange)) {
                            code = 401
                        } else {
                            body =
                                """<main><h1>1 Example Street AB1 2CD</h1><dl class="govuk-summary-list">""" +
                                """<dd>$propertyRegistration</dd></dl></main>"""
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

        private fun isAuthenticated(exchange: com.sun.net.httpserver.HttpExchange) =
            authenticated.get() && exchange.requestHeaders.getFirst("Cookie") == "SESSION=council-auth"

        override fun close() {
            server.stop(0)
            landlordServer.stop(0)
        }

        private fun query(rawQuery: String?): Map<String, String> =
            rawQuery.orEmpty().split('&').filter(String::isNotBlank).associate {
                val pair = it.split("=", limit = 2)
                URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            }
    }

    companion object {
        private val fixture =
            """[{"subject":"council-db-subject","actorName":"Fixture Council User","councilId":2,"councilName":"Fixture Council",
                "targetId":42,"targetName":"Target","targetEmail":"target@example.com","targetSubject":"target",
                "targetManager":false,"propertyId":100,"address":"1 Example Street, AB1 2CD","uprn":"100000",
                "propertyRegistrationNumber":200,"landlordId":300,"landlordName":"Fixture Landlord",
                "landlordRegistrationNumber":400,"licenseType":null}]"""
    }
}

class BasicCouncilReadContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val fixture =
            CouncilFixture.parse(
                Files.readString(Path.of(requireNotNull(properties["gatling.council.fixturePath"]))),
                1,
            ).single()
        val simulatorUrl = requireNotNull(properties["gatling.council.simulatorUrl"])
        setUp(
            scenario("Basic council read contract")
                .exec(CouncilAuthentication.chain(config, fixture.subject, simulatorUrl))
                .exec(CouncilReadJourney.basicChain(config, fixture, "BASIC: "))
                .injectOpen(atOnceUsers(1)),
        ).protocols(
            http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"),
        ).assertions(global().failedRequests().count().`is`(0L))
    }
}
