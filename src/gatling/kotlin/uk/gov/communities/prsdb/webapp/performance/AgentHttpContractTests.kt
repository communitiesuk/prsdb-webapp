package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateLicensingController
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentFrequencyAndAmountController
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AgentHttpContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `delegation submits csrf-protected forms and persists property access state`() {
        AgentServer().use { server ->
            val result = run(server)
            assertEquals(0, result.exitCode, result.output)
            assertEquals("agent-102@example.test", server.delegated[102])
            assertTrue(server.removed.isEmpty())
            assertEquals(2, server.posts.count { it["formData.emailAddress"] != null })
            assertEquals(0, server.posts.count { it["formData.wantsToProceed"] == "true" })
            assertEquals(2, server.detailsReads[102])
        }
    }

    @Test
    fun `letting-agent owner flow rejects missing csrf and an out of property form action`() {
        for (scenario in listOf("missing-csrf", "wrong-action")) {
            AgentServer(scenario).use { server ->
                val result = run(server)
                assertEquals(2, result.exitCode, result.output)
                assertTrue(server.posts.isEmpty())
                assertTrue(server.delegated.isEmpty())
            }
        }
    }

    @Test
    fun `prepare-delegation signs in each owner and leaves a family-specific delegation pending`() {
        AgentServer().use { server ->
            val ids = (101L..102L).toList()
            val families = listOf("access")
            val fixturePath = outputDirectory.resolve("agent-prepare.json")
            Files.writeString(
                fixturePath,
                ids.joinToString(",", "[", "]") { id ->
                    """{"ownerSubject":"owner-$id","propertyOwnershipId":$id,"address":"${if (id == 101L) "1" else "2"} Fixture Road"}"""
                },
            )
            val result =
                GatlingTestRunner(outputDirectory).run(
                    AgentSimulation::class.java,
                    mapOf(
                        "gatling.target" to "local",
                        "gatling.baseUrl" to server.baseUrl,
                        "gatling.simulatorUrl" to server.baseUrl,
                        "gatling.basic.landlordSubject" to "unused-subject",
                        "gatling.basic.mode" to "baseline",
                        "gatling.basic.iterations" to "1",
                        "gatling.agent.mode" to "prepare-delegation",
                        "gatling.agent.fixturePath" to fixturePath.toString(),
                    ),
                )

            assertEquals(0, result.exitCode, result.output)
            assertEquals(ids.map { "owner-$it" }, server.signedInSubjects)
            assertEquals(
                ids.mapIndexed {
                        index,
                        id,
                    ->
                    id to AgentJourney.preparationEmail(families[index % families.size], id)
                }.toMap(),
                server.delegated,
            )
            assertTrue(server.removed.isEmpty())
        }
    }

    @Test
    fun `existing-password agent enters password and completes property updates without password setup confirmation`() {
        val fixture = existingPasswordFixture()
        AgentAccessServer(listOf(fixture)).use { server ->
            val result = run(server, "existing-password", "existing-agent-password")

            assertEquals(0, result.exitCode, result.output)
            assertEquals(setOf(fixture.propertyOwnershipId), server.accessGranted)
            assertEquals(listOf("enter-password"), server.passwordFormSteps)
            assertEquals(listOf("existing-agent-password"), server.passwordSubmissions)
            assertTrue(server.setPasswordConfirmations.isEmpty())
            assertEquals(3, server.propertyDetailsReads[fixture.propertyOwnershipId])
            assertEquals("GATLING-${fixture.propertyOwnershipId}", server.updates[fixture.propertyOwnershipId]?.get("licenceNumber"))
            assertEquals("200", server.updates[fixture.propertyOwnershipId]?.get("rentAmount"))
            assertTrue(server.updates[fixture.propertyOwnershipId]?.get("rentFrequency") == "WEEKLY")
        }
    }

    @Test
    fun `incorrect existing password and invalid invitation token do not grant property access`() {
        val fixture = existingPasswordFixture()
        AgentAccessServer(listOf(fixture)).use { server ->
            val wrongPasswordResult = run(server, "wrong-password", "incorrect-password")

            assertEquals(0, wrongPasswordResult.exitCode, wrongPasswordResult.output)
            assertEquals(listOf("enter-password"), server.passwordFormSteps)
            assertEquals(listOf("incorrect-password"), server.passwordSubmissions)
            assertTrue(server.accessGranted.isEmpty())
            assertTrue(server.propertyDetailsReads.isEmpty())
            assertTrue(server.updates.isEmpty())
        }

        AgentAccessServer(emptyList()).use { server ->
            val invalidTokenResult = run(server, "invalid-token", "unused-password")

            assertEquals(0, invalidTokenResult.exitCode, invalidTokenResult.output)
            assertTrue(server.accessGranted.isEmpty())
            assertTrue(server.propertyDetailsReads.isEmpty())
        }
    }

    @Test
    fun `existing-password access selection reads its separate fixture pool and preserves unclaimed default`() {
        val unclaimedFixtures = listOf(accessFixture(301, false), accessFixture(302, false))
        val existingFixtures = listOf(accessFixture(401, true), accessFixture(402, true))
        val unclaimedPath = writeFixtures("agent-unclaimed.json", unclaimedFixtures)
        val existingPath = writeFixtures("agent-existing-password.json", existingFixtures)
        AgentAccessServer(unclaimedFixtures + existingFixtures).use { server ->
            val existingPasswordResult =
                runAccessSimulation(
                    server,
                    mapOf(
                        "gatling.agent.accessMode" to "existing-password",
                        "gatling.agent.accessFixturePath" to unclaimedPath.toString(),
                        "gatling.agent.existingPasswordFixturePath" to existingPath.toString(),
                    ),
                )

            assertEquals(0, existingPasswordResult.exitCode, existingPasswordResult.output)
            assertEquals(existingFixtures.map { it.propertyOwnershipId }.toSet(), server.accessGranted)
            assertEquals(listOf("enter-password", "enter-password"), server.passwordFormSteps)
            assertTrue(server.setPasswordConfirmations.isEmpty())
            assertTrue(server.propertyDetailsReads.keys.containsAll(existingFixtures.map { it.propertyOwnershipId }))
        }

        AgentAccessServer(unclaimedFixtures).use { server ->
            val defaultResult =
                runAccessSimulation(
                    server,
                    mapOf("gatling.agent.accessFixturePath" to unclaimedPath.toString()),
                )

            assertEquals(0, defaultResult.exitCode, defaultResult.output)
            assertEquals(unclaimedFixtures.map { it.propertyOwnershipId }.toSet(), server.accessGranted)
            assertEquals(listOf("set-password", "set-password"), server.passwordFormSteps)
            assertEquals(server.accessGranted, server.setPasswordConfirmations)
        }
    }

    private fun existingPasswordFixture() =
        AgentFixture(
            "owner-existing-password",
            AgentContractSimulation.EXISTING_PASSWORD_PROPERTY_ID,
            AgentContractSimulation.EXISTING_PASSWORD_ADDRESS,
            AgentContractSimulation.EXISTING_PASSWORD_TOKEN,
            AgentContractSimulation.EXISTING_PASSWORD_EMAIL,
            true,
        )

    private fun accessFixture(
        id: Long,
        hasPassword: Boolean,
    ) = AgentFixture(
        "owner-$id",
        id,
        "$id Fixture Road",
        "00000000-0000-0000-0000-${id.toString().padStart(12, '0')}",
        "agent-$id@example.test",
        hasPassword,
    )

    private fun writeFixtures(
        fileName: String,
        fixtures: List<AgentFixture>,
    ): Path {
        val fixturePath = outputDirectory.resolve(fileName)
        val json =
            fixtures.joinToString(",", "[", "]") { fixture ->
                """{"ownerSubject":"${fixture.ownerSubject}","propertyOwnershipId":${fixture.propertyOwnershipId},""" +
                    """"address":"${fixture.address}",""" +
                    """"token":"${fixture.token}","invitedEmail":"${fixture.invitedEmail}","hasPassword":${fixture.hasPassword}}"""
            }
        Files.writeString(fixturePath, json)
        return fixturePath
    }

    private fun run(
        server: AgentAccessServer,
        mode: String,
        password: String,
    ) = GatlingTestRunner(outputDirectory).run(
        AgentContractSimulation::class.java,
        mapOf(
            "gatling.target" to "local",
            "gatling.baseUrl" to server.baseUrl,
            "gatling.simulatorUrl" to server.baseUrl,
            "gatling.basic.landlordSubject" to "fixture-owner",
            "gatling.basic.mode" to "baseline",
            "gatling.basic.iterations" to "1",
            "gatling.agent.contractMode" to mode,
            "gatling.agent.password" to password,
        ),
    )

    private fun runAccessSimulation(
        server: AgentAccessServer,
        properties: Map<String, String>,
    ) = GatlingTestRunner(outputDirectory).run(
        AgentAccessSimulation::class.java,
        mapOf(
            "gatling.target" to "local",
            "gatling.baseUrl" to server.baseUrl,
            "gatling.simulatorUrl" to server.baseUrl,
            "gatling.basic.landlordSubject" to "fixture-owner",
            "gatling.basic.mode" to "baseline",
            "gatling.basic.iterations" to "1",
            "gatling.agent.password" to "existing-agent-password",
        ) + properties,
    )

    private fun run(server: AgentServer) =
        GatlingTestRunner(outputDirectory).run(
            AgentContractSimulation::class.java,
            mapOf(
                "gatling.target" to "local",
                "gatling.baseUrl" to server.baseUrl,
                "gatling.simulatorUrl" to server.baseUrl,
                "gatling.basic.landlordSubject" to "fixture-owner",
                "gatling.basic.mode" to "baseline",
                "gatling.basic.iterations" to "1",
            ),
        )

    private class AgentServer(
        private val scenario: String? = null,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val delegated = mutableMapOf<Long, String>()
        val removed = mutableSetOf<Long>()
        val posts = mutableListOf<Map<String, String>>()
        val detailsReads = mutableMapOf<Long, Int>()
        val signedInSubjects = mutableListOf<String>()

        init {
            server.createContext("/") { exchange ->
                if (handleSignIn(exchange)) return@createContext
                val parts = exchange.requestURI.path.split("/")
                val propertyId =
                    when (parts.getOrNull(2)) {
                        "property-details" -> parts.getOrNull(3)?.toLongOrNull()
                        "delegate-to-letting-agent" -> parts.getOrNull(3)?.toLongOrNull()
                        else -> null
                    }
                var code = 200
                var body = ""
                var location: String? = null

                fun redirect(target: String) {
                    code = 302
                    location = target
                }

                val hasJourney = exchange.requestURI.rawQuery.orEmpty().contains("journeyId=")
                val step = parts.lastOrNull().orEmpty()
                if (propertyId == null) {
                    code = 404
                } else if (exchange.requestMethod == "GET" && step in setOf("allow-letting-agent", "are-you-sure") && !hasJourney) {
                    redirect("${exchange.requestURI.path}?journeyId=${if (step == "are-you-sure") "remove" else "agent"}-$propertyId")
                } else if (parts.getOrNull(2) == "property-details" && parts.size <= 4) {
                    val id = propertyId
                    detailsReads[id] = (detailsReads[id] ?: 0) + 1
                    val email = delegated[id]?.takeUnless { id in removed }
                    val delegatePath = "/landlord/delegate-to-letting-agent/$id/allow-letting-agent"
                    val removePath = "/landlord/property-details/$id/remove-letting-agent/are-you-sure"
                    val address = if (id == 101L) "1 Fixture Road, EG1 2AA" else "2 Fixture Road, EG1 2AA"
                    body =
                        """<main><h1>$address</h1>
                        ${if (email == null) """<a href="$delegatePath">Delegate to a letting agent</a>""" else """<p>$email</p><a href="$removePath">Remove letting agent</a>"""}
                        </main>"""
                } else if (parts.getOrNull(2) == "delegate-to-letting-agent") {
                    val id = propertyId
                    if (exchange.requestMethod == "POST") {
                        val fields = parseForm(exchange)
                        posts.add(fields)
                        if (!validPost(exchange, fields, "agent-$id")) {
                            code = 403
                        } else {
                            delegated[id] = fields["formData.emailAddress"].orEmpty()
                            redirect("/landlord/delegate-to-letting-agent/$id/confirmation?journeyId=agent-$id")
                        }
                    } else if (step == "allow-letting-agent") {
                        val action = if (scenario == "wrong-action") "/outside" else ""
                        body = form(action, "formData.emailAddress", includeCsrf = scenario != "missing-csrf")
                    } else if (step == "confirmation") {
                        body =
                            "<main><h1>Delegation confirmation</h1><p>${delegated[id].orEmpty()}</p>" +
                            "<p>${if (id == 101L) "1 Fixture Road<br>EG1 2AA" else "2 Fixture Road<br>EG1 2AA"}</p></main>"
                    } else {
                        code = 404
                    }
                } else if (parts.getOrNull(2) == "property-details" && parts.getOrNull(4) == "remove-letting-agent") {
                    val id = propertyId
                    if (parts.getOrNull(5) == "are-you-sure") {
                        if (exchange.requestMethod == "POST") {
                            val fields = parseForm(exchange)
                            posts.add(fields)
                            if (!validPost(exchange, fields, "remove-$id")) {
                                code = 403
                            } else {
                                removed.add(id)
                                redirect("/landlord/property-details/$id/remove-letting-agent/confirmation?journeyId=remove-$id")
                            }
                        } else {
                            val action = ""
                            body =
                                """<main><h1>Remove letting agent</h1><p>${delegated[id].orEmpty()}</p>
                                <p>${if (id == 101L) "1 Fixture Road" else "2 Fixture Road"}</p>
                                ${form(action, "formData.wantsToProceed", includeCsrf = scenario != "missing-csrf", radio = true)}</main>"""
                        }
                    } else if (parts.getOrNull(5) == "confirmation") {
                        body =
                            "<main><h1>Removal confirmation</h1><p>${delegated[id].orEmpty()}</p>" +
                            "<p>${if (id == 101L) "1 Fixture Road<br>EG1 2AA" else "2 Fixture Road<br>EG1 2AA"}</p></main>"
                    } else {
                        code = 404
                    }
                } else {
                    code = 404
                }

                exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                if (exchange.requestHeaders.getFirst("Cookie") == null) {
                    exchange.responseHeaders.add("Set-Cookie", "SESSION=agent-session; Path=/")
                }
                location?.let { exchange.responseHeaders.add("Location", it) }
                val bytes = body.toByteArray()
                exchange.sendResponseHeaders(code, if (bytes.isEmpty()) -1 else bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }

        private fun handleSignIn(exchange: HttpExchange): Boolean {
            var location: String? = null
            var body = ""
            when (exchange.requestURI.path) {
                "/oauth2/authorization/one-login" -> location = "$baseUrl/authorize-login"
                "/authorize-login" ->
                    body =
                        """<main><form method="post" action="/simulator/login">
                        <input type="hidden" name="authCode" value="code"><input type="hidden" name="authRequestParams" value="p">
                        <input type="hidden" name="state" value="state"><input type="hidden" name="email" value="owner@example.test">
                        <input type="hidden" name="phoneNumber" value="07700900000"><input type="hidden" name="maxLoCAchieved" value="P0">
                        <textarea name="authRequestClaims">{}</textarea></form></main>"""
                "/simulator/login" -> {
                    signedInSubjects.add(parseForm(exchange).getValue("sub"))
                    location = "/landlord/login/oauth2/code/one-login?code=code"
                }
                "/landlord/login/oauth2/code/one-login" -> location = "/"
                else -> return false
            }
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            location?.let { exchange.responseHeaders.add("Location", it) }
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(if (location == null) 200 else 302, if (bytes.isEmpty()) -1 else bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
            return true
        }

        private fun validPost(
            exchange: HttpExchange,
            fields: Map<String, String>,
            journey: String,
        ) = fields["_csrf"] == "agent-csrf" &&
            exchange.requestHeaders.getFirst("Cookie") == "SESSION=agent-session" &&
            exchange.requestURI.rawQuery.orEmpty().contains("journeyId=$journey")

        private fun form(
            action: String,
            field: String,
            includeCsrf: Boolean,
            radio: Boolean = false,
        ): String {
            val csrf = if (includeCsrf) """<input type="hidden" name="_csrf" value="agent-csrf">""" else ""
            val input =
                if (radio) {
                    """<input type="radio" name="$field" value="true">"""
                } else {
                    """<input name="$field" value="">"""
                }
            return """<main><h1>Letting agent</h1><form method="post" action="$action">
                $csrf<input type="hidden" name="version" value="agent-version">$input</form></main>"""
        }

        private fun parseForm(exchange: HttpExchange): Map<String, String> =
            exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                .split("&")
                .filter { it.isNotEmpty() }
                .associate { item ->
                    val pair = item.split("=", limit = 2)
                    URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                        URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                }

        override fun close() {
            server.stop(0)
        }
    }

    private class AgentAccessServer(fixtures: List<AgentFixture>) : AutoCloseable {
        private data class Invitation(
            val fixture: AgentFixture,
            val password: String = "existing-agent-password",
            var passwordSet: Boolean = fixture.hasPassword == true,
            var accessGranted: Boolean = false,
        )

        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        private val invitations = fixtures.associate { it.token!! to Invitation(it) }
        private val journeyTokens = mutableMapOf<String, String>()
        val passwordFormSteps = mutableListOf<String>()
        val passwordSubmissions = mutableListOf<String>()
        val accessGranted = mutableSetOf<Long>()
        val setPasswordConfirmations = mutableSetOf<Long>()
        val propertyDetailsReads = mutableMapOf<Long, Int>()
        val updates = mutableMapOf<Long, MutableMap<String, String>>()

        init {
            server.createContext("/") { exchange ->
                val path = exchange.requestURI.path
                val query = queryParameters(exchange.requestURI.rawQuery)
                var code = 200
                var body = ""
                var location: String? = null

                fun redirect(target: String) {
                    code = 302
                    location = target
                }

                val invitationRoute = LettingAgentInvitationController.LETTING_AGENT_INVITATION_ROUTE
                val propertyDetailsPrefix = "/landlord/letting-agent/property-details/"
                val token = pathToken(path, propertyDetailsPrefix)
                val state = token?.let(invitations::get)

                when {
                    path == invitationRoute -> {
                        val invitationToken = query["token"]
                        val invitation = invitationToken?.let(invitations::get)
                        if (invitation == null) {
                            redirect(LettingAgentInvitationController.LETTING_AGENT_INVITATION_INVALID_LINK_ROUTE)
                        } else {
                            val journeyId = "invitation-${invitation.fixture.propertyOwnershipId}"
                            journeyTokens[journeyId] = invitationToken
                            redirect("$invitationRoute/start?journeyId=$journeyId")
                        }
                    }

                    path.startsWith("$invitationRoute/") -> {
                        val step = path.removePrefix("$invitationRoute/")
                        val invitation = query["journeyId"]?.let(journeyTokens::get)?.let(invitations::get)
                        if (invitation == null) {
                            code = 404
                        } else {
                            val propertyId = invitation.fixture.propertyOwnershipId
                            val propertyDetailsPath = propertyDetailsPrefix + invitations.entries.first { it.value == invitation }.key
                            when (step) {
                                "start" -> {
                                    val passwordStep = if (invitation.passwordSet) "enter-password" else "set-password"
                                    redirect("$invitationRoute/$passwordStep?journeyId=invitation-$propertyId")
                                }

                                "enter-password", "set-password" ->
                                    if (exchange.requestMethod == "GET") {
                                        passwordFormSteps.add(step)
                                        body = passwordForm(step == "set-password")
                                    } else {
                                        val fields = parseForm(exchange)
                                        passwordSubmissions.add(fields["password"].orEmpty())
                                        if (!validPost(fields, query) || fields["password"] != invitation.password) {
                                            body =
                                                passwordForm(
                                                    step == "set-password",
                                                    incorrectPassword = fields["password"] != invitation.password,
                                                )
                                        } else {
                                            invitation.passwordSet = true
                                            invitation.accessGranted = true
                                            accessGranted.add(propertyId)
                                            if (step == "enter-password") {
                                                redirect(propertyDetailsPath)
                                            } else {
                                                redirect("$invitationRoute/confirmation?journeyId=invitation-$propertyId")
                                            }
                                        }
                                    }

                                "confirmation" -> {
                                    setPasswordConfirmations.add(propertyId)
                                    body =
                                        "<main><h1>Property password created</h1>" +
                                        "<a href=\"$propertyDetailsPath\">Property details</a></main>"
                                }

                                else -> code = 404
                            }
                        }
                    }

                    state != null && path == propertyDetailsPrefix + token -> {
                        if (!state.accessGranted) {
                            code = 403
                        } else {
                            val fixture = state.fixture
                            val id = fixture.propertyOwnershipId
                            propertyDetailsReads[id] = (propertyDetailsReads[id] ?: 0) + 1
                            val licensingRoute =
                                LettingAgentUpdateLicensingController.LETTING_AGENT_UPDATE_LICENSING_ROUTE.replace("{token}", token!!)
                            val tenancyRoute =
                                LettingAgentUpdateRentFrequencyAndAmountController.LETTING_AGENT_UPDATE_RENT_FREQUENCY_AND_AMOUNT_ROUTE
                                    .replace("{token}", token)
                            val saved = updates[id].orEmpty()
                            body =
                                """<main><h1>${fixture.address}</h1>
                                <a href="$licensingRoute/licensing-type">Update licensing</a>
                                <a href="$tenancyRoute/rent-frequency">Update rent</a>
                                ${if (saved.containsKey("licenceNumber")) "<p>Selective licence ${saved["licenceNumber"]}</p>" else ""}
                                ${if (saved.containsKey("rentAmount")) "<p>Weekly £${saved["rentAmount"]}</p>" else ""}
                                </main>"""
                        }
                    }

                    state != null && path.startsWith("${propertyDetailsPrefix}$token/update-licensing/") -> {
                        val route =
                            LettingAgentUpdateLicensingController.LETTING_AGENT_UPDATE_LICENSING_ROUTE.replace("{token}", token!!)
                        body = handleUpdate(exchange, route, "licensing", state, query, ::redirect)
                    }

                    state != null && path.startsWith("${propertyDetailsPrefix}$token/update-rent-frequency-and-amount/") -> {
                        val route =
                            LettingAgentUpdateRentFrequencyAndAmountController.LETTING_AGENT_UPDATE_RENT_FREQUENCY_AND_AMOUNT_ROUTE
                                .replace("{token}", token!!)
                        body = handleUpdate(exchange, route, "tenancy", state, query, ::redirect)
                    }

                    else -> code = 404
                }

                exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
                if (exchange.requestHeaders.getFirst("Cookie") == null) {
                    exchange.responseHeaders.add("Set-Cookie", "SESSION=agent-access; Path=/")
                }
                location?.let { exchange.responseHeaders.add("Location", it) }
                val bytes = body.toByteArray()
                exchange.sendResponseHeaders(code, if (bytes.isEmpty()) -1 else bytes.size.toLong())
                exchange.responseBody.use { it.write(bytes) }
            }
            server.start()
        }

        private fun handleUpdate(
            exchange: com.sun.net.httpserver.HttpExchange,
            route: String,
            family: String,
            invitation: Invitation,
            query: Map<String, String>,
            redirect: (String) -> Unit,
        ): String {
            val path = exchange.requestURI.path
            val step = path.removePrefix("$route/")
            val propertyId = invitation.fixture.propertyOwnershipId
            if (!invitation.accessGranted) return ""
            if (query["journeyId"].isNullOrBlank()) {
                val journeyId = "$family-$propertyId"
                journeyTokens[journeyId] = invitation.fixture.token!!
                redirect("$path?journeyId=$journeyId")
                return ""
            }
            if (exchange.requestMethod == "GET") {
                val fields =
                    when (step) {
                        "licensing-type" -> listOf("licensingType")
                        "selective-licence" -> listOf("licenceNumber")
                        "rent-frequency" -> listOf("rentFrequency")
                        "rent-amount" -> listOf("rentAmount")
                        else -> emptyList()
                    }
                val saved = updates[propertyId].orEmpty()
                val content =
                    when (step) {
                        "check-licensing-answers" -> saved["licenceNumber"].orEmpty()
                        "rent-frequency-and-amount-check-your-answers" -> saved["rentAmount"].orEmpty()
                        else -> ""
                    }
                return updateForm(fields, content)
            }

            val submitted = parseForm(exchange)
            if (!validPost(submitted, query)) return ""
            val updatesForProperty = updates.getOrPut(propertyId) { mutableMapOf() }
            updatesForProperty.putAll(submitted.filterKeys { it !in setOf("_csrf", "version") })
            val nextStep =
                when (step) {
                    "licensing-type" -> "selective-licence"
                    "selective-licence" -> "check-licensing-answers"
                    "check-licensing-answers" -> null
                    "rent-frequency" -> "rent-amount"
                    "rent-amount" -> "rent-frequency-and-amount-check-your-answers"
                    "rent-frequency-and-amount-check-your-answers" -> null
                    else -> return ""
                }
            if (nextStep == null) {
                val propertyDetailsRoute =
                    LettingAgentPropertyDetailsController.LETTING_AGENT_PROPERTY_DETAILS_ROUTE
                        .replace("{token}", invitation.fixture.token!!)
                redirect(propertyDetailsRoute)
            } else {
                redirect("$route/$nextStep?journeyId=$family-$propertyId")
            }
            return ""
        }

        private fun passwordForm(
            includeConfirmation: Boolean,
            incorrectPassword: Boolean = false,
        ): String {
            val confirmation = if (includeConfirmation) "<input name=\"confirmPassword\" value=\"\">" else ""
            val error = if (incorrectPassword) "<p class=\"govuk-error-message\">Incorrect password</p>" else ""
            return """<main><h1>Letting agent password</h1>$error<form method="post" action="">
                <input type="hidden" name="_csrf" value="agent-csrf"><input type="hidden" name="version" value="agent-version">
                <input name="password" value="">$confirmation</form></main>"""
        }

        private fun updateForm(
            fields: List<String>,
            content: String,
        ): String {
            val inputs = fields.joinToString("") { "<input name=\"$it\" value=\"\">" }
            return """<main><h1>Letting agent property update</h1><p>$content</p><form method="post" action="">
                <input type="hidden" name="_csrf" value="agent-csrf"><input type="hidden" name="version" value="agent-version">$inputs</form></main>"""
        }

        private fun pathToken(
            path: String,
            prefix: String,
        ): String? = path.takeIf { it.startsWith(prefix) }?.removePrefix(prefix)?.substringBefore('/')?.takeIf { it.isNotBlank() }

        private fun queryParameters(rawQuery: String?): Map<String, String> =
            rawQuery.orEmpty().split('&').filter { it.isNotEmpty() }.associate { item ->
                val pair = item.split("=", limit = 2)
                URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            }

        private fun validPost(
            fields: Map<String, String>,
            query: Map<String, String>,
        ) = fields["_csrf"] == "agent-csrf" && query["journeyId"]?.isNotBlank() == true

        private fun parseForm(exchange: com.sun.net.httpserver.HttpExchange): Map<String, String> =
            exchange.requestBody.readAllBytes().toString(StandardCharsets.UTF_8)
                .split("&")
                .filter { it.isNotEmpty() }
                .associate { item ->
                    val pair = item.split("=", limit = 2)
                    URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                        URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
                }

        override fun close() {
            server.stop(0)
        }
    }
}
