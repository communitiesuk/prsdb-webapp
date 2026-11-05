package uk.gov.communities.prsdb.webapp.performance

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OffboardingHttpContractTests {
    @TempDir
    lateinit var outputDirectory: Path

    @Test
    fun `offboarding branches submit csrf protected forms and reach their outcomes`() {
        OffboardingAction.entries.forEach { action ->
            OffboardingServer(action).use { server ->
                val result =
                    GatlingTestRunner(outputDirectory).run(
                        OffboardingContractSimulation::class.java,
                        mapOf(
                            "gatling.target" to "local",
                            "gatling.baseUrl" to server.baseUrl,
                            "gatling.simulatorUrl" to server.baseUrl,
                            "gatling.basic.landlordSubject" to "contract-landlord",
                            "gatling.basic.mode" to "baseline",
                            "gatling.basic.iterations" to "1",
                            "gatling.offboarding.contract.action" to action.value,
                        ),
                    )

                assertEquals(0, result.exitCode, "${action.value}: ${result.output}")
                assertTrue(server.posts.isNotEmpty(), "${action.value} submitted no form")
                assertTrue(server.posts.all { it["_csrf"] == "offboarding-csrf" }, "${action.value} omitted CSRF")
                assertTrue(server.completed, "${action.value} did not reach its outcome")
                if (action == OffboardingAction.DEREGISTER_LANDLORD) {
                    assertTrue(server.posts.any { it["wantsToProceed"] == "true" }, "deregister-landlord did not confirm account removal")
                    assertTrue(server.reauthenticated, "deregister-landlord did not re-authenticate before confirmation")
                }
            }
        }
    }

    private class OffboardingServer(
        private val action: OffboardingAction,
    ) : AutoCloseable {
        private val server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        val baseUrl = "http://127.0.0.1:${server.address.port}"
        val posts = mutableListOf<Map<String, String>>()
        var completed = false
            private set
        var reauthenticated = false
            private set
        private val propertyId = 101L
        private val address = "1 Fixture Road<br>Testtown<br>AB1 2CD"
        private val propertyDetails =
            PropertyDetailsController.LANDLORD_PROPERTY_DETAILS_ROUTE.replace("{propertyOwnershipId}", propertyId.toString())
        private val leaveRoute = LeavePropertyController.LEAVE_PROPERTY_ROUTE.replace("{propertyOwnershipId}", propertyId.toString())
        private val switchRoute =
            SwitchToIndividualController.SWITCH_TO_INDIVIDUAL_ROUTE.replace(
                "{propertyOwnershipId}",
                propertyId.toString(),
            )
        private val deregisterPropertyRoute =
            DeregisterPropertyController.PROPERTY_DEREGISTRATION_ROUTE.replace("{propertyOwnershipId}", propertyId.toString())
        private val deregisterLandlordRoute = DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE

        init {
            server.createContext("/") { exchange -> handle(exchange) }
            server.start()
        }

        private fun handle(exchange: HttpExchange) {
            val path = exchange.requestURI.path
            var status = 200
            var body = ""
            var location: String? = null

            fun redirect(destination: String) {
                status = 302
                location = destination
            }

            val hasJourney = exchange.requestURI.rawQuery.orEmpty().contains("journeyId=")
            val initialisableEntries =
                setOf(
                    "$leaveRoute/confirm",
                    "$switchRoute/start",
                    "$deregisterPropertyRoute/deregister",
                    "$deregisterLandlordRoute/are-you-sure",
                )

            if (exchange.requestMethod == "GET" && path in initialisableEntries && !hasJourney) {
                redirect("$path?journeyId=offboarding-contract")
            } else if (exchange.requestMethod == "POST" && path == "/simulator/login") {
                val fields = parseForm(exchange)
                if (fields["sub"] == "contract-landlord" && fields["authCode"] == "code") {
                    redirect("/landlord/login/oauth2/code/one-login?code=reauth")
                } else {
                    status = 400
                }
            } else if (exchange.requestMethod == "POST") {
                val fields = parseForm(exchange)
                posts.add(fields)
                if (fields["_csrf"] != "offboarding-csrf" || !exchange.requestURI.rawQuery.orEmpty().contains("journeyId=")) {
                    status = 403
                } else {
                    when {
                        path == "$leaveRoute/confirm" -> {
                            completed = true
                            redirect("$leaveRoute/confirmation?journeyId=leave-$propertyId")
                        }
                        path == "$switchRoute/confirm" -> {
                            completed = true
                            redirect("$switchRoute/confirmation?journeyId=switch-$propertyId")
                        }
                        path == "$deregisterPropertyRoute/info" ->
                            redirect("$deregisterPropertyRoute/start?journeyId=deregister-$propertyId")
                        path == "$deregisterPropertyRoute/confirm" -> {
                            completed = true
                            redirect("$deregisterPropertyRoute/confirmation?journeyId=deregister-$propertyId")
                        }
                        // The real are-you-sure step re-renders with a validation error unless wantsToProceed=true is posted.
                        path == "$deregisterLandlordRoute/are-you-sure" && fields["wantsToProceed"] != "true" ->
                            body = """<main><div class="govuk-error-summary"><h2>There is a problem</h2></div>${form(path)}</main>"""
                        path == "$deregisterLandlordRoute/are-you-sure" -> {
                            completed = true
                            redirect("$deregisterLandlordRoute/confirmation?journeyId=landlord-removal")
                        }
                        else -> status = 404
                    }
                }
            } else {
                when {
                    path == propertyDetails -> {
                        body =
                            when (action) {
                                OffboardingAction.SWITCH_TO_INDIVIDUAL ->
                                    """<main><h1>$address</h1>
                                ${if (completed) "" else """<a href="$switchRoute/start">Switch to individual</a>"""}
                                <a href="/landlord/property-details/$propertyId/invite-joint-landlord/start">Invite a joint landlord</a></main>"""
                                OffboardingAction.DEREGISTER_PROPERTY ->
                                    """<main><h1>$address</h1>""" +
                                        """<a href="$deregisterPropertyRoute/deregister">Deregister property</a></main>"""
                                OffboardingAction.LEAVE_PROPERTY ->
                                    """<main><h1>$address</h1><a href="$leaveRoute/confirm">Leave property</a></main>"""
                                else -> """<main><h1>$address</h1></main>"""
                            }
                    }
                    path == LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE ->
                        body =
                            """<main><h1>Landlord details</h1>""" +
                            """<a href="$deregisterLandlordRoute/are-you-sure">Delete account</a></main>"""
                    path == "$switchRoute/start" -> redirect("$switchRoute/confirm?journeyId=switch-$propertyId")
                    path == "$deregisterPropertyRoute/deregister" ->
                        redirect("$deregisterPropertyRoute/info?journeyId=deregister-$propertyId")
                    path == "$deregisterPropertyRoute/start" ->
                        redirect("$deregisterPropertyRoute/confirm?journeyId=deregister-$propertyId")
                    path == "$leaveRoute/confirm" ||
                        path == "$switchRoute/confirm" ||
                        path == "$deregisterPropertyRoute/info" ||
                        path == "$deregisterPropertyRoute/confirm" ||
                        path == "$deregisterLandlordRoute/are-you-sure" -> body = form(path)
                    path == "$leaveRoute/confirmation" ->
                        body = """<main><h1>Leave property</h1><p>$address</p><p>No longer registered as a landlord</p></main>"""
                    path == "$switchRoute/confirmation" ->
                        body = """<main><h1>Switch complete</h1><p>$address</p><p>Registered as the only landlord</p></main>"""
                    path == "$deregisterPropertyRoute/confirmation" ->
                        body = """<main><h1>Deregistered</h1><p>$address</p></main>"""
                    // Deregistration clears the security context, so the real app re-authenticates before confirming.
                    path == "$deregisterLandlordRoute/confirmation" && completed && !reauthenticated ->
                        redirect("/oauth2/authorization/one-login")
                    path == "$deregisterLandlordRoute/confirmation" && completed ->
                        body = """<main><h1>Account deleted</h1></main>"""
                    path == "/oauth2/authorization/one-login" -> redirect("$baseUrl/authorize-login")
                    path == "/authorize-login" ->
                        body = """<main><form method="post" action="/simulator/login">
                            <input type="hidden" name="authCode" value="code"><input type="hidden" name="authRequestParams" value="p">
                            <input type="hidden" name="state" value="state"><input type="hidden" name="email" value="login@example.com">
                            <input type="hidden" name="phoneNumber" value="07700900000"><input type="hidden" name="maxLoCAchieved" value="P0">
                            <textarea name="authRequestClaims">{}</textarea></form></main>"""
                    path == "/landlord/login/oauth2/code/one-login" && completed -> {
                        reauthenticated = true
                        redirect("$deregisterLandlordRoute/confirmation")
                    }
                    path == LandlordController.LANDLORD_DASHBOARD_URL -> status = 403
                    else -> status = 404
                }
            }

            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            location?.let { exchange.responseHeaders.add("Location", it) }
            val bytes = body.toByteArray()
            exchange.sendResponseHeaders(status, if (bytes.isEmpty()) -1 else bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }

        private fun form(path: String): String {
            val decision =
                if (path == "$deregisterLandlordRoute/are-you-sure") {
                    """<input class="govuk-radios__input" type="radio" value="true" id="wantsToProceed" name="wantsToProceed">
                    <input class="govuk-radios__input" type="radio" value="false" id="wantsToProceed-no" name="wantsToProceed">"""
                } else {
                    ""
                }
            return """<main><h1>Confirm</h1><form method="post" action="">
                <input type="hidden" name="_csrf" value="offboarding-csrf">
                <input type="hidden" name="version" value="offboarding-version">$decision</form></main>"""
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
}
