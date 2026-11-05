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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OrganisationOperationsHttpContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `organisation administrator updates type without changing trust status and registers charity details`() {
        DetailsServer().use { server ->
            val result = runSimulation(server.baseUrl, "details")
            assertEquals(0, result.exitCode, result.output)
            assertEquals(
                listOf(listOf("COMPANY", "TRUST"), listOf("COMPANY", "CHARITY", "TRUST")),
                server.posts.filter { it.route == "update-organisation-type" && it.orgTypes().isNotEmpty() }.map { it.orgTypes() },
            )
            val charityPosts = server.posts.filter { it.route == "update-organisation-charity" }
            assertEquals(listOf("true", "true"), charityPosts.mapNotNull { it.first("charity") })
            assertEquals(
                listOf("ENGLAND_AND_WALES", "ENGLAND_AND_WALES"),
                charityPosts.mapNotNull { it.first("charityRegisteredWith") },
            )
            assertEquals(listOf("1000100", "1000101"), charityPosts.mapNotNull { it.first("charityNumber") })
            assertEquals(4, server.posts.count { it.has("answersLastChangedAt") })
            assertTrue(server.posts.all { it.first("_csrf") == "organisation-csrf" })
            assertEquals("Company, Charity, Trust", server.types)
            assertEquals("1000101", server.charityNumber)
        }
    }

    @Test
    fun `organisation details updates fail when confirmation does not persist the answers`() {
        DetailsServer(save = false).use { server ->
            val result = runSimulation(server.baseUrl, "details")
            assertEquals(2, result.exitCode, result.output)
            assertFalse(server.posts.any { it.route == "update-organisation-charity" })
        }
    }

    @Test
    fun `organisation administrator invites a team member and cancels a second invitation`() {
        TeamServer().use { server ->
            val result = runSimulation(server.baseUrl, "team")
            assertEquals(0, result.exitCode, result.output)
            assertEquals(
                setOf("org-team-contract-100-kept@example.com", "org-team-contract-101-kept@example.com"),
                server.invitations.values.toSet(),
            )
            val invites = server.posts.filter { it.has("emailAddress") }
            assertEquals(4, invites.size)
            assertTrue(invites.all { it.first("role") == "EDITOR" })
            assertEquals(2, server.posts.count { it.first("wantsToProceed") == "true" })
            assertEquals(2, server.cancelled.size)
            assertTrue(server.posts.all { it.first("_csrf") == "organisation-csrf" })
        }
    }

    @Test
    fun `team journey fails when the cancelled invitation remains pending`() {
        TeamServer(deleteOnCancel = false).use { server ->
            val result = runSimulation(server.baseUrl, "team")
            assertEquals(2, result.exitCode, result.output)
            assertEquals(1, server.posts.count { it.first("wantsToProceed") == "true" })
        }
    }

    @Test
    fun `fresh invitees accept pending invitations signing in before and after joining their organisation`() {
        MembershipServer().use { server ->
            val result = runSimulation(server.baseUrl, "accept")
            assertEquals(0, result.exitCode, result.output)
            assertEquals(listOf("invitation-100", "invitation-101"), server.acceptedTokens)
            val invitees =
                listOf(100, 101).map {
                    "urn:fdc:gov.uk:2022:gatling-org-accept-contract-$it" to "org-team-contract-$it-accepted@example.com"
                }
            assertEquals(invitees, server.members)
            assertEquals(invitees.flatMap { listOf(it.first, it.first) }, server.signIns)
            assertEquals(listOf("Gatling Invitee 100", "Gatling Invitee 101"), server.posts.mapNotNull { it.first("name") })
            assertTrue(server.posts.all { it.first("_csrf") == "organisation-csrf" })
        }
    }

    @Test
    fun `invitation acceptance fails when the invitee is not saved as an organisation member`() {
        MembershipServer(save = false).use { server ->
            val result = runSimulation(server.baseUrl, "accept")
            assertEquals(2, result.exitCode, result.output)
            assertEquals(listOf("invitation-100"), server.acceptedTokens)
        }
    }

    @Test
    fun `organisation administrators promote their first editor to administrator`() {
        MembershipServer().use { server ->
            val result = runSimulation(server.baseUrl, "change-access-level")
            assertEquals(0, result.exitCode, result.output)
            assertEquals(listOf("Fixture Admin", "Edith O'Brien", "Eddie Editor"), server.admins)
            assertTrue(server.editors.isEmpty())
            assertEquals(listOf("ADMIN", "ADMIN"), server.posts.mapNotNull { it.first("role") })
        }
    }

    @Test
    fun `organisation administrators remove their first editor from the team`() {
        MembershipServer().use { server ->
            val result = runSimulation(server.baseUrl, "remove")
            assertEquals(0, result.exitCode, result.output)
            assertEquals(listOf("Fixture Admin"), server.admins)
            assertTrue(server.editors.isEmpty())
            assertEquals(2, server.posts.count { it.route.endsWith("confirm-removal") })
            assertFalse(server.posts.any { it.has("role") })
        }
    }

    @Test
    fun `team member changes fail when the saved team still lists the editor`() {
        for (operation in listOf("change-access-level", "remove")) {
            MembershipServer(save = false).use { server ->
                val result = runSimulation(server.baseUrl, operation)
                assertEquals(2, result.exitCode, result.output)
                assertEquals(2, server.editors.size)
                assertEquals(1, server.posts.count { it.route.startsWith("team-members/") })
            }
        }
    }

    private fun runSimulation(
        baseUrl: String,
        operation: String,
    ) = GatlingTestRunner(outputDirectory).run(
        OrganisationContractSimulation::class.java,
        mapOf(
            "gatling.contract.organisation" to operation,
            "gatling.target" to "local",
            "gatling.baseUrl" to baseUrl,
            "gatling.simulatorUrl" to baseUrl,
            "gatling.basic.landlordSubject" to "synthetic-landlord",
            "gatling.basic.mode" to "baseline",
            "gatling.basic.iterations" to "1",
        ),
    )

    private data class Post(
        val route: String,
        val fields: List<Pair<String, String>>,
    ) {
        fun has(name: String) = fields.any { it.first == name }

        fun first(name: String) = fields.firstOrNull { it.first == name }?.second

        fun all(name: String) = fields.filter { it.first == name }.map { it.second }

        fun orgTypes() = fields.filter { it.first.matches(Regex("""orgTypes\[\d+]""")) }.map { it.second }
    }

    private abstract class ContractServer : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val posts = ConcurrentLinkedQueue<Post>()

        protected fun start() {
            server.createContext("/") { exchange ->
                val response = Response()
                if (exchange.requestMethod == "POST") {
                    val fields =
                        exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8).split("&").filter { it.isNotEmpty() }.map {
                            val parts = it.split("=", limit = 2)
                            URLDecoder.decode(parts[0], StandardCharsets.UTF_8) to
                                URLDecoder.decode(parts.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                        }
                    val post = Post(routeOf(exchange.requestURI.path), fields)
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

        protected abstract fun routeOf(path: String): String

        protected abstract fun handlePost(
            path: String,
            post: Post,
            response: Response,
        )

        protected abstract fun handleGet(
            exchange: HttpExchange,
            response: Response,
        )

        protected fun journeyRedirect(
            exchange: HttpExchange,
            response: Response,
        ): Boolean {
            if (exchange.requestURI.rawQuery != null) return false
            response.redirect("${exchange.requestURI.path}?journeyId=org-journey")
            return true
        }

        protected fun form(
            path: String,
            inputs: String,
        ) = """<form method="post" action="$path?journeyId=org-journey">
            <input type="hidden" name="_csrf" value="organisation-csrf">
            <input type="hidden" name="version" value="current-version">$inputs</form>"""

        protected fun row(
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

    private class DetailsServer(
        private val save: Boolean = true,
    ) : ContractServer() {
        private val details = "/landlord/landlord-details"
        private val typeRoute = "update-organisation-type"
        private val charityRoute = "update-organisation-charity"
        var types = "Company, Charity, Trust"
        var charityNumber: String? = null
        private val pending = mutableMapOf<String, List<Pair<String, String>>>()

        init {
            start()
        }

        override fun routeOf(path: String) = path.removePrefix("$details/").substringBefore('/')

        override fun handleGet(
            exchange: HttpExchange,
            response: Response,
        ) {
            val path = exchange.requestURI.path
            if (path == details) {
                exchange.responseHeaders.add("Set-Cookie", "SESSION=organisation-session; Path=/")
                val registration = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 200).toString()
                response.body = "<h1>Fixture organisation</h1>" + row("Landlord Registration Number", registration) +
                    row("Organisation address", "1 Fixture Street<br>EG1 2AA") +
                    row("Organisation type", types) + """<a href="$details/$typeRoute/organisation-type">Change</a>""" +
                    row("Registered charity", if (charityNumber == null) "No" else "Yes") +
                    """<a href="$details/$charityRoute/organisation-charity">Change</a>""" +
                    (charityNumber?.let { row("Charity number", it) } ?: "") +
                    row("Registered with Companies House", "No") +
                    """<div id="organisation-contacts">${row("Address", "Trustee Road")}</div>"""
                return
            }
            if (journeyRedirect(exchange, response)) return
            val stage = path.substringAfterLast('/')
            response.body =
                when (stage) {
                    "organisation-type" ->
                        form(
                            path,
                            // Mirrors the real th:field markup: indexed names, Spring field markers and a divider at index 3.
                            listOf(0 to "COMPANY", 1 to "CHARITY", 2 to "TRUST", 4 to "NONE").joinToString("") { (index, value) ->
                                """<input type="checkbox" name="orgTypes[$index]" value="$value">
                                <input type="hidden" name="_orgTypes[$index]" value="on">"""
                            },
                        )
                    "organisation-type-check-your-answers" ->
                        row("Organisation type", display(pending.getValue(typeRoute))) +
                            form(path, """<input type="hidden" name="answersLastChangedAt" value="2026-11-05T00:00:00Z">""")
                    "organisation-charity" ->
                        form(path, """<input type="radio" name="charity" value="true"><input type="radio" name="charity" value="false">""")
                    "organisation-charity-registered-with" ->
                        form(path, """<input type="radio" name="charityRegisteredWith" value="ENGLAND_AND_WALES">""")
                    "organisation-charity-number" -> form(path, """<input name="charityNumber">""")
                    "organisation-charity-check-your-answers" ->
                        row("Registered charity", "Yes") +
                            row("Charity number", pending.getValue(charityRoute).first { it.first == "charityNumber" }.second) +
                            form(path, """<input type="hidden" name="answersLastChangedAt" value="2026-11-05T00:00:00Z">""")
                    else -> {
                        response.code = 404
                        ""
                    }
                }
        }

        override fun handlePost(
            path: String,
            post: Post,
            response: Response,
        ) {
            val route = post.route
            val base = "$details/$route"
            when (path.substringAfterLast('/')) {
                "organisation-type" -> {
                    if (post.all("_orgTypes[0]") != listOf("on")) {
                        response.code = 400
                        return
                    }
                    val trustSelected = "TRUST" in post.orgTypes()
                    if (trustSelected != types.contains("Trust")) {
                        response.redirect("$base/organisation-type-trust-interruption?journeyId=org-journey")
                    } else {
                        pending[route] = post.fields
                        response.redirect("$base/organisation-type-check-your-answers?journeyId=org-journey")
                    }
                }
                "organisation-type-check-your-answers" -> {
                    if (save) types = display(pending.getValue(route))
                    response.redirect(details)
                }
                "organisation-charity" -> response.redirect("$base/organisation-charity-registered-with?journeyId=org-journey")
                "organisation-charity-registered-with" -> response.redirect("$base/organisation-charity-number?journeyId=org-journey")
                "organisation-charity-number" -> {
                    pending[route] = post.fields
                    response.redirect("$base/organisation-charity-check-your-answers?journeyId=org-journey")
                }
                "organisation-charity-check-your-answers" -> {
                    if (save) charityNumber = pending.getValue(route).first { it.first == "charityNumber" }.second
                    response.redirect(details)
                }
                else -> response.code = 404
            }
        }

        private fun display(fields: List<Pair<String, String>>) =
            fields.filter { it.first.matches(Regex("""orgTypes\[\d+]""")) }.map { it.second }.map {
                mapOf("COMPANY" to "Company", "CHARITY" to "Charity", "TRUST" to "Trust", "NONE" to "Other").getValue(it)
            }.joinToString(", ")
    }

    private class TeamServer(
        private val deleteOnCancel: Boolean = true,
    ) : ContractServer() {
        private val team = "/landlord/team-members"
        val invitations = linkedMapOf<Long, String>()
        val cancelled = mutableListOf<String>()
        private var nextId = 40L

        init {
            start()
        }

        override fun routeOf(path: String) = path.removePrefix("$team/").substringBefore('/')

        override fun handleGet(
            exchange: HttpExchange,
            response: Response,
        ) {
            val path = exchange.requestURI.path
            if (path == team) {
                exchange.responseHeaders.add("Set-Cookie", "SESSION=organisation-session; Path=/")
                response.body =
                    """<h1 class="govuk-heading-l">Team members</h1><a href="$team/invite/invite-team-member">Invite</a>""" +
                    """<div id="invitations"><dl class="govuk-summary-list">""" +
                    invitations.entries.joinToString("") { (id, email) ->
                        """<div class="govuk-summary-list__row"><dt class="govuk-summary-list__key">Invitation</dt>
                            <dd class="govuk-summary-list__value">$email</dd><dd class="govuk-summary-list__actions">
                            <a href="$team/resend/$id">Resend</a><a href="$team/cancel-invitation/$id/are-you-sure">Cancel</a></dd></div>"""
                    } + "</dl></div>"
                return
            }
            if (journeyRedirect(exchange, response)) return
            response.body =
                when {
                    path == "$team/invite/invite-team-member" ->
                        form(
                            path,
                            """<input name="emailAddress"><input type="radio" name="role" value="ADMIN">""" +
                                """<input type="radio" name="role" value="EDITOR">""",
                        )
                    path.startsWith("$team/cancel-invitation/") && path.endsWith("/are-you-sure") ->
                        form(
                            path,
                            """<input type="radio" name="wantsToProceed" value="true">""" +
                                """<input type="radio" name="wantsToProceed" value="false">""",
                        )
                    else -> {
                        response.code = 404
                        ""
                    }
                }
        }

        override fun handlePost(
            path: String,
            post: Post,
            response: Response,
        ) {
            when {
                path == "$team/invite/invite-team-member" -> {
                    invitations[nextId++] = requireNotNull(post.first("emailAddress"))
                    response.redirect(team)
                }
                path.startsWith("$team/cancel-invitation/") && post.first("wantsToProceed") == "true" -> {
                    val id = path.removePrefix("$team/cancel-invitation/").substringBefore('/').toLong()
                    if (deleteOnCancel) invitations.remove(id)?.let { cancelled.add(it) }
                    response.redirect("$team#invitations")
                }
                else -> response.code = 404
            }
        }
    }

    private class MembershipServer(
        private val save: Boolean = true,
    ) : ContractServer() {
        private val team = "/landlord/team-members"
        private val accept = "/landlord/accept-invitation"
        val admins = mutableListOf("Fixture Admin")
        val editors = linkedMapOf(7L to "Edith O'Brien", 8L to "Eddie Editor")
        val acceptedTokens = mutableListOf<String>()
        val members = mutableListOf<Pair<String, String>>()
        val signIns = mutableListOf<String>()
        private var token: String? = null
        private var subject: String? = null
        private var authenticated = false
        private var member = false
        private var banner = ""
        private val answers = mutableMapOf<String, String>()

        init {
            start()
        }

        override fun routeOf(path: String) = path.removePrefix("/landlord/").removePrefix("/")

        override fun handleGet(
            exchange: HttpExchange,
            response: Response,
        ) {
            val path = exchange.requestURI.path
            val signIn = "$baseUrl/oauth2/authorization/one-login"
            when {
                path == accept -> {
                    exchange.responseHeaders.add("Set-Cookie", "SESSION=organisation-session; Path=/")
                    token = exchange.requestURI.rawQuery?.removePrefix("token=")?.takeIf { it.startsWith("invitation-") }
                    authenticated = false
                    member = false
                    if (token == null) response.code = 404 else response.redirect("$accept/validate-token?journeyId=org-journey")
                }
                path == "$accept/validate-token" -> response.redirect("$accept/check-if-user-is-landlord?journeyId=org-journey")
                path == "$accept/check-if-user-is-landlord" ->
                    response.redirect(if (authenticated) "$accept/join-your-organisation?journeyId=org-journey" else signIn)
                path == "/oauth2/authorization/one-login" -> response.redirect("$baseUrl/authorize-login")
                path == "/authorize-login" ->
                    response.body =
                        form(
                            "/simulator/login",
                            """<input type="hidden" name="authCode" value="code"><input type="hidden" name="authRequestParams" value="p">
                            <input type="hidden" name="state" value="state"><input type="hidden" name="email" value="login@example.com">
                            <input type="hidden" name="phoneNumber" value="07700900000"><input type="hidden" name="maxLoCAchieved" value="P0">
                            <textarea name="authRequestClaims">{}</textarea>""",
                        )
                path == "/login/oauth2/code/one-login" -> response.redirect("/")
                path == "$accept/confirmation" ->
                    if (!authenticated) {
                        response.redirect(signIn)
                    } else {
                        response.body = """<p id="joined-organisation">You have joined Fixture organisation.</p>
                            <p id="confirmation-email">We have sent a confirmation email to ${answers["emailAddress"]}</p>"""
                    }
                path == "/landlord/landlord-details" ->
                    if (authenticated && member) {
                        val registrationNumber = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, 200).toString()
                        response.body = row("Landlord Registration Number", registrationNumber)
                    } else {
                        response.code = 403
                    }
                path == team -> {
                    exchange.responseHeaders.add("Set-Cookie", "SESSION=organisation-session; Path=/")
                    response.body = teamPage()
                    banner = ""
                }
                journeyRedirect(exchange, response) -> return
                path == "$accept/join-your-organisation" ->
                    response.body = """<p class="govuk-body">You have been invited as part of Fixture organisation.</p>""" + form(path, "")
                path == "$accept/name" -> response.body = form(path, """<input name="name">""")
                path == "$accept/email" -> response.body = form(path, """<input type="email" name="emailAddress">""")
                path == "$accept/check-answers" ->
                    response.body = row("Name", answers.getValue("name")) + row("Email", answers.getValue("emailAddress")) + form(path, "")
                path.endsWith("/change-access-level/access-level") ->
                    response.body =
                        form(path, """<input type="radio" name="role" value="ADMIN"><input type="radio" name="role" value="EDITOR">""") +
                        """<a class="govuk-link" href="${path.removeSuffix("access-level")}confirm-removal">Remove access</a>"""
                path.endsWith("/change-access-level/confirm-removal") -> response.body = form(path, "")
                else -> response.code = 404
            }
        }

        override fun handlePost(
            path: String,
            post: Post,
            response: Response,
        ) {
            val journey = "?journeyId=org-journey"
            when {
                path == "/simulator/login" -> {
                    subject = post.first("sub")?.also { signIns.add(it) }
                    authenticated = true
                    response.redirect("/login/oauth2/code/one-login?code=login")
                }
                path.startsWith(accept) && !authenticated -> response.code = 403
                path == "$accept/join-your-organisation" -> response.redirect("$accept/name$journey")
                path == "$accept/name" -> {
                    answers["name"] = requireNotNull(post.first("name"))
                    response.redirect("$accept/email$journey")
                }
                path == "$accept/email" -> {
                    answers["emailAddress"] = requireNotNull(post.first("emailAddress"))
                    response.redirect("$accept/check-answers$journey")
                }
                path == "$accept/check-answers" -> {
                    acceptedTokens.add(requireNotNull(token))
                    if (save) {
                        member = true
                        members.add(requireNotNull(subject) to answers.getValue("emailAddress"))
                    }
                    authenticated = false
                    response.redirect("$accept/confirmation")
                }
                path.endsWith("/change-access-level/access-level") && post.first("role") == "ADMIN" -> {
                    val id = memberId(path)
                    banner = bannerHtml("Access level changed", "${editors.getValue(id)} is now an administrator.")
                    if (save) admins.add(editors.remove(id)!!)
                    response.redirect(team)
                }
                path.endsWith("/change-access-level/confirm-removal") -> {
                    val id = memberId(path)
                    banner = bannerHtml("Access removed", "${editors.getValue(id)} no longer has access to the service.")
                    if (save) editors.remove(id)
                    response.redirect(team)
                }
                else -> response.code = 404
            }
        }

        private fun memberId(path: String) = path.removePrefix("$team/").substringBefore('/').toLong()

        private fun escape(text: String) = text.replace("'", "&#39;")

        private fun bannerHtml(
            heading: String,
            content: String,
        ) = """<div class="govuk-notification-banner"><div class="govuk-notification-banner__content">
            <h3 class="govuk-notification-banner__heading">$heading</h3><p class="govuk-body">${escape(content)}</p></div></div>"""

        private fun teamPage() =
            banner + """<h1 class="govuk-heading-l">Team members</h1>
                <a class="govuk-tabs__tab" href="#administrators">Administrators (${admins.size})</a>
                <a class="govuk-tabs__tab" href="#editors">Editors (${editors.size})</a>
                <div class="govuk-tabs__panel" id="administrators"><dl class="govuk-summary-list">""" +
                admins.joinToString("") { row("Administrator", escape(it)) } +
                """</dl></div><div class="govuk-tabs__panel" id="editors"><dl class="govuk-summary-list">""" +
                editors.entries.joinToString("") { (id, name) ->
                    """<div class="govuk-summary-list__row"><dt class="govuk-summary-list__key">Editor</dt>
                    <dd class="govuk-summary-list__value">${escape(name)}</dd><dd class="govuk-summary-list__actions">
                    <a href="$team/$id/change-access-level/access-level">Change</a></dd></div>"""
                } + "</dl></div>"
    }
}
