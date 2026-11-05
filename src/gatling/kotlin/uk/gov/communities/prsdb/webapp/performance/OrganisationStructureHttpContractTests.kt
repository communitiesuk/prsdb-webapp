package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OrganisationStructureHttpContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `organisation administrator replaces the lead trustee of a trust`() {
        val server = runSuccessfully("lead-trustee")
        assertEquals(
            listOf("Performance Lead Trustee 100", "Performance Lead Trustee 101"),
            server.saved.map { it.leadTrustee },
        )
        assertEquals(listOf("lead-trustee-100@example.com", "lead-trustee-101@example.com"), server.saved.map { it.leadTrusteeEmail })
        assertEquals(listOf("1980"), server.posts.mapNotNull { it.first("year") }.distinct())
        assertEquals(listOf("02079460125"), server.posts.mapNotNull { it.first("phoneNumber") }.distinct())
    }

    @Test
    fun `organisation administrator adds a governing body member alongside existing members`() {
        val server = runSuccessfully("governing-body")
        assertEquals(
            listOf(
                listOf("Existing Member", "Performance Governing Member 100"),
                listOf("Existing Member", "Performance Governing Member 101"),
            ),
            server.saved.map { it.members },
        )
        assertEquals(listOf("TRUSTEE", "TRUSTEE"), server.posts.mapNotNull { it.first("whoToProvide") })
    }

    @Test
    fun `registered company changes its Companies House number`() {
        val server = runSuccessfully("company-number")
        assertEquals(listOf("PF000100", "PF000101"), server.saved.map { it.companyNumber })
        assertEquals(0, server.posts.count { it.path.endsWith("organisation-companies-house-interruption") })
    }

    @Test
    fun `non-company organisation registers with Companies House and loses its governing body`() {
        val server = runSuccessfully("to-company")
        assertEquals(listOf("PF000100", "PF000101"), server.saved.map { it.companyNumber })
        assertEquals(listOf(emptyList<String>(), emptyList()), server.saved.map { it.members })
        assertEquals(2, server.posts.count { it.path.endsWith("organisation-companies-house-interruption") })
    }

    @Test
    fun `registered company deregisters from Companies House and provides a governing body`() {
        val server = runSuccessfully("to-non-company")
        assertEquals(listOf(null, null), server.saved.map { it.companyNumber })
        assertEquals(
            listOf(listOf("Performance Governing Member 100"), listOf("Performance Governing Member 101")),
            server.saved.map { it.members },
        )
    }

    @Test
    fun `organisation becomes a trust and provides a lead trustee`() {
        val server = runSuccessfully("add-trust")
        assertEquals(listOf(listOf("CHARITY", "TRUST"), listOf("CHARITY", "TRUST")), server.saved.map { it.types })
        assertEquals(
            listOf("Performance Lead Trustee 100", "Performance Lead Trustee 101"),
            server.saved.map { it.leadTrustee },
        )
    }

    @Test
    fun `trust stops being a trust and its lead trustee is removed`() {
        val server = runSuccessfully("remove-trust")
        assertEquals(listOf(listOf("NONE"), listOf("NONE")), server.saved.map { it.types })
        assertEquals(listOf(null, null), server.saved.map { it.leadTrustee })
    }

    @Test
    fun `structure updates fail when confirmation does not persist the answers`() {
        for (operation in listOf("lead-trustee", "governing-body", "to-non-company", "remove-trust")) {
            StructureServer(operation, save = false).use { server ->
                val result = runSimulation(server.baseUrl, operation)
                assertEquals(2, result.exitCode, "$operation: ${result.output}")
            }
        }
    }

    private fun runSuccessfully(operation: String): StructureServer =
        StructureServer(operation).use { server ->
            val result = runSimulation(server.baseUrl, operation)
            assertEquals(0, result.exitCode, result.output)
            assertTrue(server.posts.all { it.first("_csrf") == "organisation-csrf" })
            assertEquals(2, server.saved.size, "Each fixture must reach a saved details page")
            server
        }

    private fun runSimulation(
        baseUrl: String,
        operation: String,
    ) = GatlingTestRunner(outputDirectory).run(
        OrganisationContractSimulation::class.java,
        mapOf(
            "gatling.contract.organisation" to "structure:$operation",
            "gatling.target" to "local",
            "gatling.baseUrl" to baseUrl,
            "gatling.simulatorUrl" to baseUrl,
            "gatling.basic.landlordSubject" to "synthetic-landlord",
            "gatling.basic.mode" to "baseline",
            "gatling.basic.iterations" to "1",
        ),
    )

    private data class Post(
        val path: String,
        val fields: List<Pair<String, String>>,
    ) {
        fun first(name: String) = fields.firstOrNull { it.first == name }?.second

        fun orgTypes() = fields.filter { it.first.matches(Regex("""orgTypes\[\d+]""")) }.map { it.second }
    }

    private data class Organisation(
        val types: List<String>,
        val leadTrustee: String? = null,
        val leadTrusteeEmail: String? = null,
        val members: List<String> = emptyList(),
        val companyNumber: String? = null,
    )

    // Simulates the real update journeys: journeyId redirects, internal routing steps and stateful confirmation.
    private class StructureServer(
        operation: String,
        private val save: Boolean = true,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val posts = ConcurrentLinkedQueue<Post>()
        val saved = mutableListOf<Organisation>()
        private val details = "/landlord/landlord-details"
        private val leadTrusteeRoute = "$details/update-lead-trustee"
        private val governingBodyRoute = "$details/update-governing-body"
        private val companiesHouseRoute = "$details/update-companies-house"
        private val typeRoute = "$details/update-organisation-type"
        private val selected = "1 PRSDB Performance Square, EG1 2AA"
        private val initial =
            when (operation) {
                "lead-trustee" -> Organisation(listOf("CHARITY", "TRUST"), "Old Trustee", "old@example.com", listOf("Existing Member"))
                "company-number", "to-non-company" -> Organisation(listOf("COMPANY"), companyNumber = "12345678")
                "remove-trust" -> Organisation(listOf("TRUST"), "Old Trustee", "old@example.com", listOf("Existing Member"))
                else -> Organisation(listOf("CHARITY"), members = listOf("Existing Member"))
            }
        private var organisation = initial
        private var savedSinceDetails = false
        private var detailsServedAfterSave = false
        private val pending = mutableMapOf<String, String>()
        private var pendingMembers = mutableListOf<String>()
        private var pendingTypes = emptyList<String>()
        private var registered = false

        init {
            server.createContext("/") { exchange ->
                val response = Response()
                if (exchange.requestMethod == "POST") {
                    val fields =
                        exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").filter { it.isNotEmpty() }.map {
                            val parts = it.split("=", limit = 2)
                            URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                    val post = Post(exchange.requestURI.path, fields)
                    posts.add(post)
                    if (post.first("_csrf") != "organisation-csrf" || post.first("version") != "current-version" ||
                        exchange.requestHeaders.getFirst("Cookie") != "SESSION=organisation-session" ||
                        exchange.requestURI.rawQuery != "journeyId=org-journey"
                    ) {
                        response.code = 403
                    } else {
                        handlePost(exchange.requestURI.path, post, response)
                    }
                } else {
                    handleGet(exchange, response)
                }
                exchange.responseHeaders.add("Content-Type", "text/html")
                response.location?.let { exchange.responseHeaders.add("Location", it) }
                val bytes = response.body.toByteArray(StandardCharsets.UTF_8)
                exchange.sendResponseHeaders(response.code, if (bytes.isEmpty()) -1 else bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }

        private fun handleGet(
            exchange: HttpExchange,
            response: Response,
        ) {
            val path = exchange.requestURI.path
            if (path == details) {
                // Each fixture starts from the original organisation once the previous fixture has seen its saved details.
                if (detailsServedAfterSave) {
                    organisation = initial
                    detailsServedAfterSave = false
                }
                if (savedSinceDetails) {
                    saved.add(organisation)
                    savedSinceDetails = false
                    detailsServedAfterSave = true
                }
                exchange.responseHeaders.add("Set-Cookie", "SESSION=organisation-session; Path=/")
                response.body = detailsBody()
                return
            }
            if (exchange.requestURI.rawQuery == null) {
                response.redirect("$path?journeyId=org-journey")
                return
            }
            if (path == "$governingBodyRoute/start") {
                pendingMembers = organisation.members.toMutableList()
                response.redirect("$governingBodyRoute/organisation-governing-body-member-list?journeyId=org-journey")
                return
            }
            response.body =
                when (path.substringAfterLast('/')) {
                    "lead-trustee-name", "organisation-governing-body-member-name" -> form(path, """<input name="name">""")
                    "lead-trustee-dob", "organisation-governing-body-member-dob" ->
                        form(path, """<input name="day"><input name="month"><input name="year">""")
                    "lead-trustee-email" -> form(path, """<input name="emailAddress">""")
                    "lead-trustee-phone" -> form(path, """<input name="phoneNumber">""")
                    "lookup-address" -> form(path, """<input name="postcode"><input name="houseNameOrNumber">""")
                    "select-address" ->
                        form(
                            path,
                            """<input type="radio" name="address" value="$selected">""" +
                                """<input type="radio" name="address" value="MANUAL">""",
                        )
                    "organisation-governing-body-who-to-provide" ->
                        form(
                            path,
                            listOf("DIRECTOR", "TRUSTEE", "PARTNER", "OTHER").joinToString("") {
                                """<input type="radio" name="whoToProvide" value="$it">"""
                            },
                        )
                    "organisation-governing-body-member-list" ->
                        pendingMembers.joinToString("") { row("Name", it) } +
                            """<a href="organisation-governing-body-who-to-provide?journeyId=org-journey" """ +
                            """role="button" draggable="false" class="govuk-button govuk-button--secondary">Add another</a>""" +
                            form(path, "")
                    "organisation-companies-house" ->
                        form(
                            path,
                            """<input type="radio" name="companiesHouse" value="true">""" +
                                """<input type="radio" name="companiesHouse" value="false">""",
                        )
                    "organisation-companies-house-interruption", "organisation-type-trust-interruption" -> form(path, "")
                    "organisation-company-number" -> form(path, """<input name="companyNumber">""")
                    "organisation-type" ->
                        form(
                            path,
                            listOf(0 to "COMPANY", 1 to "CHARITY", 2 to "TRUST", 4 to "NONE").joinToString("") { (index, value) ->
                                """<input type="checkbox" name="orgTypes[$index]" value="$value">""" +
                                    """<input type="hidden" name="_orgTypes[$index]" value="on">"""
                            },
                        )
                    "lead-trustee-check-your-answers",
                    "governing-body-check-your-answers",
                    "check-answers",
                    "organisation-type-check-your-answers",
                    ->
                        pending.entries.joinToString("") { row(it.key, it.value) } + pendingMembers.joinToString("") { row("Name", it) } +
                            (if (pendingTypes.isEmpty()) "" else row("Organisation type", display(pendingTypes))) +
                            form(path, """<input type="hidden" name="answersLastChangedAt" value="2026-11-05T00:00:00Z">""")
                    else -> {
                        response.code = 404
                        ""
                    }
                }
        }

        private fun handlePost(
            path: String,
            post: Post,
            response: Response,
        ) {
            val route =
                path.substringBeforeLast(
                    '/',
                ).removeSuffix("/lead-trustee-address").removeSuffix("/organisation-governing-body-member-address")

            fun next(segment: String) = response.redirect("$route/$segment?journeyId=org-journey")
            when (path.substringAfterLast('/')) {
                "lead-trustee-name" -> {
                    pending["Lead trustee name"] = requireNotNull(post.first("name"))
                    next("lead-trustee-dob")
                }
                "lead-trustee-dob" -> if (post.first("year") == "1980") next("lead-trustee-email") else response.code = 400
                "lead-trustee-email" -> {
                    pending["Lead trustee email"] = requireNotNull(post.first("emailAddress"))
                    next("lead-trustee-phone")
                }
                "lead-trustee-phone" -> next("lead-trustee-address/lookup-address")
                "lookup-address" ->
                    response.redirect("${path.substringBeforeLast('/')}/select-address?journeyId=org-journey")
                "select-address" ->
                    when {
                        post.first("address") != selected -> response.code = 400
                        path.contains("organisation-governing-body-member-address") -> {
                            pendingMembers.add(pending.getValue("Member name"))
                            next("organisation-governing-body-member-list")
                        }
                        route == typeRoute -> next("organisation-type-check-your-answers")
                        else -> next("lead-trustee-check-your-answers")
                    }
                "organisation-governing-body-who-to-provide" ->
                    if (post.first("whoToProvide") in setOf("DIRECTOR", "TRUSTEE", "PARTNER", "OTHER")) {
                        next("organisation-governing-body-member-name")
                    } else {
                        response.code = 400
                    }
                "organisation-governing-body-member-name" -> {
                    pending["Member name"] = requireNotNull(post.first("name"))
                    next("organisation-governing-body-member-dob")
                }
                "organisation-governing-body-member-dob" -> next("organisation-governing-body-member-address/lookup-address")
                "organisation-governing-body-member-list" ->
                    next(if (route == governingBodyRoute) "governing-body-check-your-answers" else "check-answers")
                "organisation-companies-house" -> {
                    registered = post.first("companiesHouse") == "true"
                    when {
                        registered != (organisation.companyNumber != null) -> next("organisation-companies-house-interruption")
                        registered -> next("organisation-company-number")
                        else -> response.code = 404
                    }
                }
                "organisation-companies-house-interruption" ->
                    if (registered) {
                        next("organisation-company-number")
                    } else {
                        pendingMembers = mutableListOf()
                        next("organisation-governing-body-who-to-provide")
                    }
                "organisation-company-number" -> {
                    val number = post.first("companyNumber").orEmpty()
                    if (number.matches(Regex("[A-Za-z0-9]{8}"))) {
                        pending["Companies House number"] = number
                        next("check-answers")
                    } else {
                        response.code = 400
                    }
                }
                "organisation-type" -> {
                    if (post.fields.none { it.first == "_orgTypes[0]" }) {
                        response.code = 400
                        return
                    }
                    pendingTypes = post.orgTypes()
                    next(
                        if (("TRUST" in pendingTypes) != ("TRUST" in organisation.types)) {
                            "organisation-type-trust-interruption"
                        } else {
                            "organisation-type-check-your-answers"
                        },
                    )
                }
                "organisation-type-trust-interruption" ->
                    next(if ("TRUST" in pendingTypes) "lead-trustee-name" else "organisation-type-check-your-answers")
                "lead-trustee-check-your-answers" ->
                    confirm(response, "$details#organisation-contacts") {
                        it.copy(leadTrustee = pending["Lead trustee name"], leadTrusteeEmail = pending["Lead trustee email"])
                    }
                "governing-body-check-your-answers" ->
                    confirm(response, "$details#organisation-contacts") { it.copy(members = pendingMembers.toList()) }
                "check-answers" ->
                    confirm(response, details) {
                        if (registered) {
                            it.copy(companyNumber = pending["Companies House number"], members = emptyList())
                        } else {
                            it.copy(companyNumber = null, members = pendingMembers.toList())
                        }
                    }
                "organisation-type-check-your-answers" ->
                    confirm(response, details) {
                        if ("TRUST" in pendingTypes) {
                            it.copy(
                                types = pendingTypes,
                                leadTrustee = pending["Lead trustee name"],
                                leadTrusteeEmail = pending["Lead trustee email"],
                            )
                        } else {
                            it.copy(types = pendingTypes, leadTrustee = null, leadTrusteeEmail = null)
                        }
                    }
                else -> response.code = 404
            }
        }

        private fun confirm(
            response: Response,
            location: String,
            update: (Organisation) -> Organisation,
        ) {
            if (save) organisation = update(organisation)
            savedSinceDetails = true
            pending.clear()
            pendingMembers = mutableListOf()
            pendingTypes = emptyList()
            response.redirect(location)
        }

        private fun detailsBody(): String {
            val registration = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 200).toString()
            val trust = "TRUST" in organisation.types
            return "<h1>Fixture organisation</h1>" + row("Landlord Registration Number", registration) +
                row("Organisation type", display(organisation.types)) + """<a href="$typeRoute/organisation-type">Change</a>""" +
                row("Registered with Companies House", if (organisation.companyNumber == null) "No" else "Yes") +
                (organisation.companyNumber?.let { row("Companies House number", it) } ?: "") +
                """<a href="$companiesHouseRoute/organisation-companies-house">Change</a>""" +
                """<div id="organisation-contacts"><h2 class="govuk-heading-m">Main contact</h2>""" +
                """<div class="govuk-summary-card">${row("Name", "Main Contact")}</div>""" +
                (
                    if (trust) {
                        """<h2 class="govuk-heading-m">Lead trustee</h2><div class="govuk-summary-card">""" +
                            """<a href="$leadTrusteeRoute/lead-trustee-name">Change</a>""" +
                            row("Name", organisation.leadTrustee.orEmpty()) +
                            row("Email", organisation.leadTrusteeEmail.orEmpty()) + "</div>"
                    } else {
                        ""
                    }
                ) +
                (
                    if (organisation.companyNumber == null) {
                        """<h2 class="govuk-heading-m">Governing body</h2><a href="$governingBodyRoute/start">Members</a>""" +
                            organisation.members.joinToString("") { """<div class="govuk-summary-card">${row("Name", it)}</div>""" }
                    } else {
                        ""
                    }
                ) + "</div>"
        }

        private fun display(types: List<String>) =
            types.joinToString(
                ", ",
            ) { mapOf("COMPANY" to "Company", "CHARITY" to "Charity", "TRUST" to "Trust", "NONE" to "Other").getValue(it) }

        private fun form(
            path: String,
            inputs: String,
        ) = """<form method="post" action="$path?journeyId=org-journey">
            <input type="hidden" name="_csrf" value="organisation-csrf">
            <input type="hidden" name="version" value="current-version">$inputs</form>"""

        private fun row(
            label: String,
            value: String,
        ) = """<div class="govuk-summary-list__row"><dt class="govuk-summary-list__key">$label</dt>
            <dd class="govuk-summary-list__value">$value</dd></div>"""

        override fun close() = server.stop(0)

        class Response {
            var code = 200
            var body = ""
            var location: String? = null

            fun redirect(target: String) {
                code = 302
                location = target
            }
        }
    }
}
