package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpServer
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegistrationHttpContractTests {
    private enum class OwnershipPath {
        ASSOCIATED_PARTIES,
        LEGACY,
        NONE,
    }

    private val paidVariants =
        listOf(
            RegistrationVariant.VACANT_MANUAL_EXEMPT, RegistrationVariant.OCCUPIED_SELECTED_CERTIFICATES,
            RegistrationVariant.OCCUPIED_MANUAL_EXPIRED, RegistrationVariant.OCCUPIED_SELECTED_MISSING,
            RegistrationVariant.OCCUPIED_MANUAL_DEFERRED, RegistrationVariant.OCCUPIED_SELECTED_LOW_RATING,
            RegistrationVariant.OCCUPIED_MANUAL_SUPERSEDED, RegistrationVariant.OCCUPIED_SELECTED_NOT_FOUND,
            RegistrationVariant.OCCUPIED_SELECTED_UPRN,
        )

    @TestFactory
    fun `paid variants traverse their correlated branches and local contract terminal`() =
        paidVariants.map { variant ->
            dynamicTest(variant.name) {
                RegistrationServer(variant = variant).use { server ->
                    val result = run(server, variant)
                    assertEquals(0, result.exitCode, "$variant: ${result.output}")
                    assertEquals(server.expectedSteps, server.postedSteps.toList())
                    assertTrue(server.posts.all { it["_csrf"] == "registration-csrf" })
                    assertEquals(emptyList(), server.rejections.toList())
                    assertEquals(variant.occupied.toString(), server.posts.single { "occupied" in it }["occupied"])
                    assertEquals(
                        if (variant.manual) "MANUAL" else "1 Fixture Road, FA1 1AA",
                        server.posts.first { "address" in it }["address"],
                    )
                    assertEquals(1, server.cardVisits)
                    assertEquals(2, server.persistedReads)
                    assertEquals(2, server.paymentReturnReads)
                    assertEquals(
                        listOf("journeyId=journey-a&paymentReference=payment-reference"),
                        server.paymentReturnQueries.distinct(),
                    )
                    assertEquals("${RegistrationDestination.ROUTE}/confirmation", server.completionLocation)
                    assertEquals(
                        (if (variant.gas == "upload") 1 else 0) + (if (variant.electrical.endsWith("upload")) 1 else 0),
                        server.certificateDownloads,
                    )
                }
            }
        }

    @TestFactory
    fun `paid branches reject unsafe forms and destinations before payment`() =
        paidVariants.map { variant ->
            dynamicTest(variant.name) {
                for (defect in listOf("branch-csrf", "branch-action", "branch-redirect", "branch-hidden", "branch-status")) {
                    RegistrationServer(defect, variant).use { server ->
                        val result = run(server, variant)
                        assertEquals(2, result.exitCode, "$variant $defect: ${result.output}")
                        assertEquals(server.expectedSteps.takeWhile { it != "check-epc-answers" }, server.postedSteps.toList())
                        assertEquals(0, server.cardVisits, "$variant $defect reached payment")
                        assertEquals(0, server.persistedReads)
                    }
                }
            }
        }

    @Test
    fun `completes real correlated tasks and hosted payment and reloads persisted property`() {
        RegistrationServer().use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertTrue(server.posts.all { it["_csrf"] == "registration-csrf" })
            val ownershipPost = server.posts.single { it.containsKey("ownershipKinds[0]") }
            assertEquals("FREEHOLD", ownershipPost["ownershipKinds[0]"])
            assertTrue("ownershipKinds" !in ownershipPost)
            assertTrue("ownershipKinds[1]" !in ownershipPost)
            assertTrue(server.posts.any { it["exemptionReason"] == "ANNUAL_USE_LESS_THAN_4_MONTHS" })
            assertEquals(1, server.cardVisits)
            assertEquals(2, server.persistedReads)
        }
    }

    @Test
    fun `warm-up and measured property lookup requests retain their prefixes`() {
        for (prefix in listOf("Warm-up: ", "Concurrent paid: ")) {
            RegistrationServer().use { server ->
                val directory = Files.createDirectories(Path.of("build/registration-contract-tests", UUID.randomUUID().toString()))
                val result = run(server, prefix = prefix, directory = directory)
                assertEquals(0, result.exitCode, result.output)
                val log =
                    Files.walk(directory).use { paths ->
                        val path = paths.filter { it.fileName.toString() == "simulation.log" }.findFirst().orElseThrow()
                        String(Files.readAllBytes(path), Charsets.ISO_8859_1)
                    }
                assertTrue(log.contains("${prefix}Registration: find registered property"), "Missing prefixed property lookup")
            }
        }
    }

    @Test
    fun `paid registration confirms the quoted prorated fee`() {
        RegistrationServer(quotedPayment = "£23.45").use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(1, server.cardVisits)
            assertEquals(2, server.persistedReads)
        }
    }

    @Test
    fun `paid registration rejects a confirmation with a different payment amount`() {
        RegistrationServer(defect = "confirmation-payment").use { server ->
            val result = run(server)
            assertEquals(2, result.exitCode, result.output)
            assertEquals(1, server.cardVisits)
            assertEquals(0, server.persistedReads)
        }
    }

    @Test
    fun `legacy ownership route completes a paid basic registration`() {
        RegistrationServer(ownershipPath = OwnershipPath.LEGACY).use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals(server.expectedSteps, server.postedSteps.toList())
            assertEquals("FREEHOLD", server.posts.single { "ownershipType" in it }["ownershipType"])
            assertTrue("ownership-type" in server.postedSteps)
            assertTrue("has-joint-landlords" in server.postedSteps)
            assertTrue(
                server.postedSteps.none {
                    it in setOf("owns-property", "ownership-kind", "freehold-type", "property-manager")
                },
            )
            assertEquals(1, server.cardVisits)
        }
    }

    @Test
    fun `registration does not reach payment without either complete ownership route`() {
        RegistrationServer(ownershipPath = OwnershipPath.NONE).use { server ->
            val result = run(server)
            assertEquals(2, result.exitCode, result.output)
            assertEquals(0, server.cardVisits)
            assertTrue("payment-summary" !in server.postedSteps)
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
        for (defect in listOf("property", "compliance", "confirmation-title")) {
            RegistrationServer(defect).use { server ->
                val result = run(server)
                assertEquals(2, result.exitCode, "$defect: ${result.output}")
                assertEquals(1, server.cardVisits)
            }
        }
    }

    private fun run(
        server: RegistrationServer,
        variant: RegistrationVariant = RegistrationVariant.VACANT_MANUAL_EXEMPT,
        prefix: String = "",
        directory: Path = Files.createDirectories(Path.of("build/registration-contract-tests", UUID.randomUUID().toString())),
    ): GatlingTestRunner.Result {
        val certificate = Files.writeString(directory.resolve("contract-certificate.pdf"), CONTRACT_CERTIFICATE)
        return GatlingTestRunner(directory).run(
            RegistrationContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "synthetic-landlord",
                "gatling.basic.mode" to "baseline",
                "gatling.basic.iterations" to "1",
                "gatling.contract.registrationVariant" to variant.name,
                "gatling.contract.certificatePath" to certificate.toAbsolutePath().toString(),
                "gatling.contract.prefix" to prefix,
            ),
        )
    }

    private class RegistrationServer(
        private val defect: String? = null,
        private val variant: RegistrationVariant = RegistrationVariant.VACANT_MANUAL_EXEMPT,
        private val ownershipPath: OwnershipPath = OwnershipPath.ASSOCIATED_PARTIES,
        private val quotedPayment: String = "£65",
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val posts = ConcurrentLinkedQueue<Map<String, String>>()
        val postedSteps = ConcurrentLinkedQueue<String>()
        val rejections = ConcurrentLinkedQueue<String>()
        var cardVisits = 0
        var persistedReads = 0
        var paymentReturnReads = 0
        var certificateDownloads = 0
        val paymentReturnQueries = ConcurrentLinkedQueue<String>()
        var completionLocation: String? = null
        private var registered = false
        private val route = RegistrationDestination.ROUTE
        private val tasks =
            listOf(
                "lookup-address",
                "has-electrical-safety",
                if (variant.manual) "has-epc" else "check-epc-details",
                "check-answers",
            )
        private val forms =
            linkedMapOf<String, List<String>>().apply {
                putAll(
                    linkedMapOf(
                        "lookup-address" to listOf("postcode", "houseNameOrNumber"),
                        "select-address" to listOf("address"),
                    ),
                )
                if (variant.manual) {
                    put("manual-address", listOf("addressLineOne", "townOrCity", "postcode"))
                    put("local-council", listOf("localCouncilId"))
                }
                put("property-type", listOf("propertyType"))
                put("number-of-bedrooms", listOf("numberOfBedrooms"))
                when (ownershipPath) {
                    OwnershipPath.ASSOCIATED_PARTIES -> {
                        put("has-joint-landlords", listOf("hasJointLandlords"))
                        put("owns-property", listOf("ownsProperty"))
                        put("ownership-kind", listOf("ownershipKinds[0]", "ownershipKinds[1]"))
                        put("freehold-type", listOf("freeholdType"))
                        put("property-manager", listOf("usingPropertyManager"))
                    }

                    OwnershipPath.LEGACY -> {
                        put("ownership-type", listOf("ownershipType"))
                        put("has-joint-landlords", listOf("hasJointLandlords"))
                    }

                    OwnershipPath.NONE -> put("has-joint-landlords", listOf("hasJointLandlords"))
                }
                putAll(
                    linkedMapOf(
                        "correspondence-email" to listOf("correspondenceEmailOption"),
                        "correspondence-address/lookup-address" to listOf("postcode", "houseNameOrNumber"),
                        "correspondence-address/select-address" to listOf("address"),
                        "correspondence-address/manual-address" to listOf("addressLineOne", "townOrCity", "postcode"),
                        "occupancy" to listOf("occupied"),
                        "licensing-type" to listOf(if (variant.licensing == "PROVIDE_LATER") "action" else "licensingType"),
                    ),
                )
                when (variant.licensing) {
                    "SELECTIVE_LICENCE" -> put("selective-licence", listOf("licenceNumber"))
                    "HMO_MANDATORY_LICENCE" -> put("hmo-mandatory-licence", listOf("licenceNumber"))
                    "HMO_ADDITIONAL_LICENCE" -> put("hmo-additional-licence", listOf("licenceNumber"))
                    "PROVIDE_LATER" -> put("provide-licensing-later", emptyList())
                }
                if (variant.occupied) {
                    put("who-provides-rental-details", listOf("whoProvides"))
                    put("number-of-households", listOf(if (variant.gas == "later") "action" else "numberOfHouseholds"))
                    if (variant.gas == "later") {
                        put("provide-tenancy-details-later", emptyList())
                    } else {
                        putAll(
                            linkedMapOf(
                                "number-of-people" to listOf("numberOfPeople"),
                                "rent-includes-bills" to listOf("rentIncludesBills"),
                                "property-furnished" to listOf("furnishedStatus"),
                                "rent-frequency" to listOf("rentFrequency"),
                                "rent-amount" to listOf("rentAmount"),
                            ),
                        )
                    }
                }
                put("has-gas-supply", listOf(if (variant.gas == "later") "action" else "hasGasSupply"))
                when (variant.gas) {
                    "later" -> put("provide-gas-safety-certificate-later", emptyList())
                    "missing" -> {
                        put("has-gas-safety", listOf("hasCert"))
                        put("gas-safety-certificate-missing", emptyList())
                    }

                    "upload", "expired" -> {
                        put("has-gas-safety", listOf("hasCert"))
                        put("gas-safety-certificate-issue-date", listOf("day", "month", "year"))
                        if (variant.gas == "upload") {
                            put("gas-safety-certificate-file-upload", emptyList())
                            put("check-gas-safety-certificate-uploads", emptyList())
                        } else {
                            put("gas-safety-certificate-expired", emptyList())
                        }
                    }
                }
                put("check-gas-safety-answers", emptyList())
                put("has-electrical-safety", listOf(if (variant.electrical == "later") "action" else "electricalCertType"))
                when (variant.electrical) {
                    "later" -> put("provide-electrical-safety-certificate-later", emptyList())
                    "missing" -> put("electrical-safety-certificate-missing", emptyList())
                    else -> {
                        val type = if (variant.electrical.startsWith("eicr")) "eicr" else "eic"
                        put("$type-expiry-date", listOf("day", "month", "year"))
                        if (variant.electrical.endsWith("upload")) {
                            put("$type-file-upload", emptyList())
                            put("check-$type-uploads", emptyList())
                        } else {
                            put("$type-expired", emptyList())
                        }
                    }
                }
                put("check-electrical-safety-answers", emptyList())
                if (!variant.manual) put("check-epc-details", listOf("matchedEpcIsCorrect"))
                if (variant.epc != "uprn") {
                    put("has-epc", listOf(if (variant.epc == "later") "action" else "hasCert"))
                    when (variant.epc) {
                        "exempt", "missing" -> {
                            put("is-epc-required", listOf("epcRequired"))
                            put(
                                if (variant.epc == "exempt") "epc-exemption" else "epc-missing",
                                if (variant.epc == "exempt") listOf("exemptionReason") else emptyList(),
                            )
                        }

                        "later" -> put("provide-epc-later", emptyList())
                        else -> {
                            put("find-your-epc", listOf("certificateNumber"))
                            if (variant.epc == "not-found") {
                                put("epc-not-found", emptyList())
                                put("is-epc-required", listOf("epcRequired"))
                                put("epc-missing", emptyList())
                            } else {
                                if (variant.epc == "superseded") {
                                    put("epc-superseded", emptyList())
                                } else {
                                    put("confirm-epc-details", listOf("matchedEpcIsCorrect"))
                                }
                                if (variant.epc == "expired") {
                                    put("epc-in-date-at-start-of-tenancy-check", listOf("tenancyStartedBeforeExpiry"))
                                    put("epc-expired", emptyList())
                                }
                                if (variant.epc == "low") {
                                    put("has-mees-exemption", listOf("propertyHasExemption"))
                                    put("mees-exemption", listOf("exemptionReason"))
                                }
                            }
                        }
                    }
                }
                put("check-epc-answers", emptyList())
                put("check-answers", emptyList())
                put("payment-summary", emptyList())
            }
        val expectedSteps = forms.keys.toList()
        private val address = if (variant.manual) "NFT registration 901, Performance Town, FA1 1AA" else "1 Fixture Road, FA1 1AA"

        private fun certificateNumber(mode: String) = "${if (variant.manual) "1000" else "0000"}-0000-0000-0000-901$mode"

        private fun expectedValues(step: String): Map<String, String> {
            if (step == "ownership-kind") return mapOf("ownershipKinds[0]" to "FREEHOLD")
            val today = LocalDate.now(ZoneId.of("Europe/London"))
            val date =
                when {
                    step == "gas-safety-certificate-issue-date" -> today.minusMonths(if (variant.gas == "expired") 24 else 1)
                    variant.electrical.endsWith("expired") -> today.minusDays(5)
                    else -> today.plusYears(2)
                }
            return forms.getValue(step).associateWith { field ->
                when (field) {
                    "postcode" -> "FA1 1AA"
                    "houseNameOrNumber" -> "1"
                    "address" -> if (step.startsWith("correspondence-address/") || variant.manual) "MANUAL" else address
                    "addressLineOne" -> if (step.startsWith("correspondence-address/")) "NFT correspondence 901" else "NFT registration 901"
                    "townOrCity" -> "Performance Town"
                    "localCouncilId" -> "1"
                    "propertyType" -> "DETACHED_HOUSE"
                    "numberOfBedrooms" -> "3"
                    "ownershipType" -> "FREEHOLD"
                    "hasJointLandlords", "rentIncludesBills", "tenancyStartedBeforeExpiry" -> "false"
                    "ownsProperty" -> "true"
                    "freeholdType" -> "SOLE_OWNERSHIP"
                    "usingPropertyManager" -> "false"
                    "correspondenceEmailOption" -> "ACCOUNT_EMAIL"
                    "occupied" -> variant.occupied.toString()
                    "licensingType" -> variant.licensing
                    "licenceNumber" -> "NFT-901"
                    "whoProvides" -> "LANDLORD"
                    "numberOfHouseholds" -> "1"
                    "numberOfPeople" -> "2"
                    "furnishedStatus" -> "FURNISHED"
                    "rentFrequency" -> "MONTHLY"
                    "rentAmount" -> "950"
                    "action" -> "provideThisLater"
                    "hasGasSupply" -> (variant.gas != "none").toString()
                    "hasCert" ->
                        (
                            if (step == "has-gas-safety") {
                                variant.gas != "missing"
                            } else {
                                variant.epc !in
                                    setOf(
                                        "missing",
                                        "exempt",
                                    )
                            }
                        ).toString()

                    "electricalCertType" ->
                        when (variant.electrical) {
                            "missing" -> "NO_CERTIFICATE"
                            "eicr-upload", "eicr-expired" -> "HAS_EICR"
                            else -> "HAS_EIC"
                        }

                    "day" -> date.dayOfMonth.toString()
                    "month" -> date.monthValue.toString()
                    "year" -> date.year.toString()
                    "matchedEpcIsCorrect" -> (step == "confirm-epc-details" || variant.epc == "uprn").toString()
                    "certificateNumber" ->
                        certificateNumber(
                            when (variant.epc) {
                                "expired" -> "2"
                                "low" -> "3"
                                "not-found" -> "4"
                                "superseded" -> "5"
                                else -> "1"
                            },
                        )

                    "epcRequired" -> (variant.epc != "exempt").toString()
                    "exemptionReason" -> if (step == "mees-exemption") "HIGH_COST" else "ANNUAL_USE_LESS_THAN_4_MONTHS"
                    "propertyHasExemption" -> "true"
                    else -> error("Missing contract expectation for $step: $field")
                }
            }
        }

        private fun licensingLabel() =
            when (variant.licensing) {
                "SELECTIVE_LICENCE" -> "Selective licence"
                "HMO_MANDATORY_LICENCE" -> "HMO mandatory licence"
                "HMO_ADDITIONAL_LICENCE" -> "HMO additional licence"
                "PROVIDE_LATER" -> "Provide this later"
                else -> "None"
            }

        private fun complianceCards(): String {
            val gas =
                when (variant.gas) {
                    "none" -> "Does the property have a gas supply or any gas appliances? No"
                    "upload" -> "Valid performance-certificate.pdf"
                    "expired" -> "Expired"
                    "later" -> "Provide this later"
                    else -> "Do you have a gas safety certificate for this property? No"
                }
            val electrical =
                when {
                    !variant.occupied || variant.electrical == "later" -> "Provide this later"
                    variant.electrical.endsWith(
                        "upload",
                    ) -> "Valid performance-certificate.pdf (${if (variant.electrical.startsWith("eicr")) "EICR" else "EIC"})"
                    variant.electrical.endsWith("expired") -> "Expired"
                    else -> "None"
                }
            val epc =
                when (variant.epc) {
                    "valid", "uprn", "superseded" -> "${certificateNumber("1")} Valid"
                    "expired" -> "${certificateNumber("2")} Expired"
                    "low" -> "${certificateNumber("3")} High cost"
                    "exempt" -> "less than 4 months a year"
                    "later" -> "Provide this later"
                    else -> "Do you have an EPC for this property? No"
                }
            return """<div id="compliance-information" role="tabpanel" hidden>""" +
                """<div class="govuk-summary-card">Gas safety certificate $gas</div>""" +
                """<div class="govuk-summary-card">Electrical safety certificate $electrical</div>""" +
                """<div class="govuk-summary-card">Energy performance certificate (EPC) $epc</div></div>"""
        }

        init {
            server.createContext("/") { exchange ->
                val uri = exchange.requestURI
                val path = uri.path
                val current = path.removePrefix("$route/")
                var code = 200
                var body = ""
                var contentType = "text/html; charset=utf-8"

                fun redirect(destination: String) {
                    code = 302
                    exchange.responseHeaders.add("Location", destination)
                }

                fun location(step: String) = "$route/$step?journeyId=journey-a"

                fun cookiePresent() = exchange.requestHeaders.getFirst("Cookie").orEmpty().contains("REGISTRATION=actor-a")
                if (path == "$route/task-list" && uri.rawQuery == null) {
                    if (defect != "cookie") exchange.responseHeaders.add("Set-Cookie", "REGISTRATION=actor-a; Path=/; HttpOnly")
                    redirect(
                        location("task-list").replace(
                            "journey-a",
                            if (defect == "redirect") "journey-b&journeyId=journey-a" else "journey-a",
                        ),
                    )
                } else if (!cookiePresent()) {
                    code = 403
                } else if (exchange.requestMethod == "POST") {
                    val rawBody = exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                    val multipart = current.endsWith("file-upload")
                    val fields =
                        if (multipart) {
                            val csrf = Regex("name=\"_csrf\"\\r\\n\\r\\n([^\\r]+)").find(rawBody)?.groupValues?.get(1).orEmpty()
                            mapOf("_csrf" to csrf)
                        } else {
                            rawBody.split('&').associate { value ->
                                val pair = value.split('=', limit = 2)
                                URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                                    URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                            }
                        }
                    posts.add(fields)
                    postedSteps.add(current)
                    val expected =
                        expectedValues(current) + ("_csrf" to "registration-csrf") +
                            if (multipart) emptyMap() else mapOf("contractState" to "state-$current")
                    if (current != expectedSteps.getOrNull(postedSteps.size - 1) || fields != expected ||
                        (
                            multipart && (
                                !rawBody.contains("filename=\"performance-certificate.pdf\"") ||
                                    !rawBody.contains(CONTRACT_CERTIFICATE) ||
                                    !exchange.requestHeaders.getFirst("Content-Type").orEmpty().startsWith("multipart/form-data;")
                            )
                        )
                    ) {
                        rejections.add("$current: $fields, expected $expected")
                        code = 403
                    } else if (current == "payment-summary") {
                        redirect("/local/gov-uk-pay/mock-card-page/payment-one")
                    } else if (current in listOf("check-gas-safety-answers", "check-electrical-safety-answers", "check-epc-answers")) {
                        redirect(location("task-list"))
                    } else {
                        redirect(location(forms.keys.toList()[forms.keys.indexOf(current) + 1]))
                    }
                } else if (path == "/local/gov-uk-pay/mock-card-page/payment-one") {
                    cardVisits++
                    registered = true
                    redirect(location("payment-return") + "&paymentReference=payment-reference")
                } else if (current == "payment-return") {
                    paymentReturnReads++
                    paymentReturnQueries.add(uri.rawQuery.orEmpty())
                    if (registered && paymentReturnReads == 1) {
                        body = """<div id="payment-status-no-js-content"></div>"""
                    } else {
                        completionLocation = if (registered) "$route/confirmation" else location("payment-failed")
                        redirect(requireNotNull(completionLocation))
                    }
                } else if (current == "payment-failed") {
                    body = """<h1>Payment failed</h1><a href="${location("payment-summary")}">Try again</a>"""
                } else if (current == "task-list") {
                    body = tasks.joinToString("") { """<a class="govuk-task-list__link" href="${location(it)}">$it</a>""" }
                } else if (current == "confirmation" && registered) {
                    val heading = if (defect == "confirmation-title") "Registration complete" else "Property registered"
                    body =
                        """<main><div class="govuk-panel"><h1 class="govuk-panel__title">$heading</h1>$address</div>""" +
                        """<strong>P-1234-5678</strong><dl><dt>Payment</dt>""" +
                        """<dd>${if (defect == "confirmation-payment") "£30" else quotedPayment}</dd>""" +
                        """<dt>Payment date</dt><dd>21 November 2026</dd><dt>Renewal date</dt><dd>21 November 2027</dd></dl></main>"""
                } else if (path == "/landlord/landlord-details") {
                    body =
                        if (registered && defect != "property") {
                            """<table><tr><td><a href="/landlord/property-details/100?withBackUrl=17">$address</a></td></tr></table>"""
                        } else {
                            "No properties"
                        }
                } else if (path == "/landlord/property-details/100") {
                    persistedReads++
                    body = """<h1>$address</h1>""" +
                        """<div class="govuk-summary-list__row">Number of bedrooms 3</div>""" +
                        """<div class="govuk-summary-list__row">Licensing type ${licensingLabel()}</div>""" +
                        """<div class="govuk-summary-list__row">Contact address NFT correspondence 901</div>""" +
                        """<div class="govuk-summary-list__row">Is this property occupied by tenants? """ +
                        """${if (variant.occupied) "Yes" else "No"}</div>""" +
                        (
                            if (variant.occupied && variant.gas != "later") {
                                """<div class="govuk-summary-list__row">Number of households 1</div>""" +
                                    """<div class="govuk-summary-list__row">Number of tenants 2</div>""" +
                                    """<div class="govuk-summary-list__row">Rent amount £950.00</div>"""
                            } else {
                                ""
                            }
                        ) +
                        if (defect == "compliance") {
                            ""
                        } else {
                            complianceCards()
                        }
                } else if (path == "/contract-certificate.pdf") {
                    certificateDownloads++
                    contentType = "application/pdf"
                    body = CONTRACT_CERTIFICATE
                } else if (current == "check-epc-answers" && defect == "branch-redirect") {
                    redirect(location("check-epc-answers").replace("journey-a", "wrong-journey"))
                } else if (current == "check-epc-answers" && defect == "branch-status") {
                    code = 500
                } else if (current in forms) {
                    val branchFault = current == "check-epc-answers"
                    val action =
                        if (branchFault && defect == "branch-action") {
                            location("payment-summary")
                        } else {
                            location(current).replace("journey-a", if (defect == "action") "wrong-journey" else "journey-a")
                        }
                    val upload = current.endsWith("file-upload")
                    if (upload) {
                        exchange.responseHeaders.add(
                            "Set-Cookie",
                            "file-upload-cookie=contract-token; Path=$path; Secure; HttpOnly",
                        )
                    }
                    val formInputs =
                        if (current == "ownership-kind") {
                            """<input type="checkbox" name="ownershipKinds[0]" value="FREEHOLD">""" +
                                """<input type="checkbox" name="ownershipKinds[1]" value="LEASEHOLD">"""
                        } else {
                            forms.getValue(current).joinToString("") { """<input name="$it">""" }
                        }
                    body = """<form method="post" action="$action">""" +
                        (
                            if (defect == "csrf" || (branchFault && defect == "branch-csrf")) {
                                ""
                            } else {
                                """<input type="hidden" name="_csrf" value="registration-csrf">"""
                            }
                        ) +
                        (if (branchFault && defect == "branch-hidden") """<input type="hidden" name="_csrf" value="duplicate">""" else "") +
                        (if (upload) "" else """<input type="hidden" name="contractState" value="state-$current">""") +
                        formInputs + "</form>" +
                        (
                            if (current == "payment-summary") {
                                """<dl><dt>Payment</dt><dd>$quotedPayment</dd><dt>Payment date</dt><dd>21 November 2026</dd>""" +
                                    """<dt>Renewal date</dt><dd>21 November 2027</dd></dl>"""
                            } else {
                                ""
                            }
                        ) +
                        (if (current == "eic-expired") "<h1>You need a different electrical safety certificate</h1>" else "") +
                        (
                            if (RegistrationJourney.isUploadCheck(current)) {
                                """<table><tbody class="govuk-table__body"><tr><td>""" +
                                    """<a class="govuk-link" href="/contract-certificate.pdf">""" +
                                    """performance-certificate.pdf</a></td></tr></tbody></table>"""
                            } else {
                                ""
                            }
                        )
                } else {
                    code = 404
                }
                exchange.responseHeaders.add("Content-Type", contentType)
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

class RegistrationContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val fixture = RegistrationFixture("synthetic-landlord", "1 Fixture Road, FA1 1AA", "FA1 1AA", "1", 1, 901)
        setUp(
            scenario("Full registration HTTP contract")
                .exec(
                    RegistrationJourney.chain(
                        config,
                        fixture,
                        properties["gatling.contract.registrationVariant"]?.let(RegistrationVariant::valueOf)
                            ?: RegistrationVariant.VACANT_MANUAL_EXEMPT,
                        "${config.baseUrl}/local/gov-uk-pay",
                        properties["gatling.contract.certificatePath"],
                        prefix = properties["gatling.contract.prefix"].orEmpty(),
                    ),
                ).injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    RegistrationJourney.requiredRequests.map { properties["gatling.contract.prefix"].orEmpty() + it },
                ),
            )
    }
}

// Bytes for the in-process HTTP contract only; no real scanner or certificate service is exercised.
private const val CONTRACT_CERTIFICATE = "%PDF-1.4\nSynthetic registration HTTP contract\n%%EOF"
