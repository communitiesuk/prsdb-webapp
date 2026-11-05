package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.bodyString
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import uk.gov.communities.prsdb.webapp.controllers.DeregisterLandlordController
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.controllers.LandlordController
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.controllers.PropertyDetailsController
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController
import java.net.URI

object OffboardingJourney {
    fun requestNames(action: OffboardingAction): List<String> =
        when (action) {
            OffboardingAction.LEAVE_PROPERTY ->
                listOf("property details", "start leaving", "confirm leaving", "submit leave", "leave confirmation")
            OffboardingAction.SWITCH_TO_INDIVIDUAL ->
                listOf(
                    "property details",
                    "start switch",
                    "check pending invitations",
                    "confirm switch",
                    "submit switch",
                    "switch confirmation",
                    "updated property",
                )
            OffboardingAction.DEREGISTER_PROPERTY ->
                listOf(
                    "property details",
                    "start deregistration",
                    "check can deregister",
                    "deregistration information",
                    "continue deregistration",
                    "check pending invitations",
                    "confirm deregistration",
                    "submit deregistration",
                    "deregistration confirmation",
                )
            OffboardingAction.DEREGISTER_LANDLORD ->
                listOf(
                    "landlord details",
                    "start account removal",
                    "account removal form",
                    "confirm account removal",
                    "account removal requires sign-in",
                    "refresh authentication",
                    "account removal confirmation",
                    "removed account access",
                )
        }.map { "Offboarding ${action.value}: $it" } +
            if (action == OffboardingAction.DEREGISTER_LANDLORD) {
                listOf(OneLoginAuthentication.loginRequestNames(REAUTHENTICATION_PREFIX)[1])
            } else {
                emptyList()
            }

    // Deregistration saves an empty security context, so the confirmation page sends the browser back through One Login.
    private const val REAUTHENTICATION_PREFIX = "Offboarding deregister-landlord reauthentication"
    private const val SIGN_IN = "/oauth2/authorization/one-login"

    fun chain(
        config: BasicRunConfig,
        fixture: OffboardingFixture,
        prefix: String = "",
    ): ChainBuilder {
        val names = requestNames(fixture.action).map { prefix + it }.iterator()
        return when (fixture.action) {
            OffboardingAction.LEAVE_PROPERTY -> leaveProperty(config, fixture, names)
            OffboardingAction.SWITCH_TO_INDIVIDUAL -> switchToIndividual(config, fixture, names)
            OffboardingAction.DEREGISTER_PROPERTY -> deregisterProperty(config, fixture, names)
            OffboardingAction.DEREGISTER_LANDLORD -> deregisterLandlord(config, fixture, names, prefix)
        }
    }

    private fun leaveProperty(
        config: BasicRunConfig,
        fixture: OffboardingFixture,
        names: Iterator<String>,
    ): ChainBuilder {
        val id = requireNotNull(fixture.propertyOwnershipId)
        val details = PropertyDetailsController.LANDLORD_PROPERTY_DETAILS_ROUTE.replace("{propertyOwnershipId}", id.toString())
        val route = LeavePropertyController.LEAVE_PROPERTY_ROUTE.replace("{propertyOwnershipId}", id.toString())
        val firstStep = "$route/confirm"
        return exec(propertyDetails(config, names.next(), details, firstStep))
            .exec(initialise(config, names.next(), firstStep))
            .exec(getForm(config, names.next(), route, "confirm", null))
            .exec(submitForm(config, names.next(), route, emptyMap(), "$route/confirmation"))
            .exec(
                http(names.next())
                    .get("#{offNext}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(bodyString().transform { JointLandlordJourney.containsAddress(it, requireNotNull(fixture.address)) }.`is`(true))
                    .check(bodyString().transform { it.contains("No longer registered as a landlord") }.`is`(true)),
            ).exitHereIfFailed()
    }

    private fun switchToIndividual(
        config: BasicRunConfig,
        fixture: OffboardingFixture,
        names: Iterator<String>,
    ): ChainBuilder {
        val id = requireNotNull(fixture.propertyOwnershipId)
        val details = PropertyDetailsController.LANDLORD_PROPERTY_DETAILS_ROUTE.replace("{propertyOwnershipId}", id.toString())
        val route = SwitchToIndividualController.SWITCH_TO_INDIVIDUAL_ROUTE.replace("{propertyOwnershipId}", id.toString())
        val firstStep = "$route/start"
        return exec(propertyDetails(config, names.next(), details, firstStep))
            .exec(initialise(config, names.next(), firstStep))
            .exec(redirectingStep(config, names.next(), route, "confirm"))
            .exec(getForm(config, names.next(), route, "confirm", null))
            .exec(submitForm(config, names.next(), route, emptyMap(), "$route/confirmation"))
            .exec(
                http(names.next())
                    .get("#{offNext}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(bodyString().transform { JointLandlordJourney.containsAddress(it, requireNotNull(fixture.address)) }.`is`(true))
                    .check(bodyString().transform { it.contains("Registered as the only landlord") }.`is`(true)),
            ).exitHereIfFailed()
            .exec(
                http(names.next())
                    .get { HttpDestination.withCacheBuster(config.baseUrl + details) }
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(css("a[href*='$firstStep']").notExists())
                    .check(css("a[href*='/invite-joint-landlord/start']").exists()),
            ).exitHereIfFailed()
    }

    private fun deregisterProperty(
        config: BasicRunConfig,
        fixture: OffboardingFixture,
        names: Iterator<String>,
    ): ChainBuilder {
        val id = requireNotNull(fixture.propertyOwnershipId)
        val details = PropertyDetailsController.LANDLORD_PROPERTY_DETAILS_ROUTE.replace("{propertyOwnershipId}", id.toString())
        val route = DeregisterPropertyController.PROPERTY_DEREGISTRATION_ROUTE.replace("{propertyOwnershipId}", id.toString())
        val firstStep = "$route/deregister"
        return exec(propertyDetails(config, names.next(), details, firstStep))
            .exec(initialise(config, names.next(), firstStep))
            .exec(redirectingStep(config, names.next(), route, "info"))
            .exec(getForm(config, names.next(), route, "info", null))
            .exec(submitForm(config, names.next(), route, emptyMap(), "$route/start"))
            .exec(redirectingStep(config, names.next(), route, "confirm"))
            .exec(getForm(config, names.next(), route, "confirm", null))
            .exec(submitForm(config, names.next(), route, emptyMap(), "$route/confirmation"))
            .exec(
                http(names.next())
                    .get("#{offNext}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(bodyString().transform { JointLandlordJourney.containsAddress(it, requireNotNull(fixture.address)) }.`is`(true))
                    .check(bodyString().transform { it.contains("Deregistered") }.`is`(true)),
            ).exitHereIfFailed()
    }

    private fun deregisterLandlord(
        config: BasicRunConfig,
        fixture: OffboardingFixture,
        names: Iterator<String>,
        prefix: String,
    ): ChainBuilder {
        val details = LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
        val route = DeregisterLandlordController.LANDLORD_DEREGISTRATION_ROUTE
        val firstStep = DeregisterLandlordController.LANDLORD_DEREGISTRATION_PATH
        return exec(propertyDetails(config, names.next(), details, firstStep))
            .exec(initialise(config, names.next(), firstStep))
            .exec(getForm(config, names.next(), route, "are-you-sure", "wantsToProceed"))
            .exec(submitForm(config, names.next(), route, mapOf("wantsToProceed" to "true"), "$route/confirmation"))
            .exec { session -> session.set("offConfirmation", requireNotNull(session.getString("offNext"))) }
            .exec(
                http(names.next())
                    .get("#{offConfirmation}")
                    .disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(
                        header("Location").transform { location ->
                            val resolved = HttpDestination.resolve(location, config.baseUrl)
                            require(
                                URI.create(resolved).path == SIGN_IN,
                            ) { "Account removal confirmation did not require sign-in: $resolved" }
                            resolved
                        }.saveAs("offSignIn"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http(names.next())
                    .get("#{offSignIn}")
                    .disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(
                        header("Location")
                            .transform { HttpDestination.resolve(it, config.simulatorUrl) }
                            .saveAs(OneLoginAuthentication.authorizationUrlKey(REAUTHENTICATION_PREFIX)),
                    ),
            ).exitHereIfFailed()
            .exec(
                OneLoginAuthentication.authenticationCompletionChain(
                    config,
                    fixture.subject,
                    REAUTHENTICATION_PREFIX,
                    requestLabelPrefix = prefix,
                ),
            )
            .exec(
                http(names.next())
                    .get("#{offConfirmation}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(bodyString().transform { it.contains("Account deleted") }.`is`(true)),
            ).exitHereIfFailed()
            .exec(
                http(names.next())
                    .get(LandlordController.LANDLORD_DASHBOARD_URL)
                    .disableFollowRedirect()
                    .check(status().`is`(403)),
            ).exitHereIfFailed()
    }

    private fun propertyDetails(
        config: BasicRunConfig,
        name: String,
        details: String,
        firstStep: String,
    ): ChainBuilder =
        exec(
            http(name)
                .get { HttpDestination.withCacheBuster(config.baseUrl + details) }
                .disableFollowRedirect()
                .check(status().`is`(200))
                .check(css("a[href*='$firstStep']", "href").transform { HttpDestination.resolve(it, config.baseUrl) }.saveAs("offNext")),
        ).exitHereIfFailed()

    private fun initialise(
        config: BasicRunConfig,
        name: String,
        firstStep: String,
    ): ChainBuilder =
        exec(
            http(name)
                .get("#{offNext}")
                .disableFollowRedirect()
                .check(status().`is`(302))
                .check(header("Location").transform { JointAgentJourneySupport.initialisedStep(it, config, firstStep) }.saveAs("offNext")),
        ).exitHereIfFailed()

    private fun redirectingStep(
        config: BasicRunConfig,
        name: String,
        route: String,
        nextSegment: String,
    ): ChainBuilder =
        exec(
            http(name)
                .get("#{offNext}")
                .disableFollowRedirect()
                .check(status().`is`(302))
                .check(header("Location").transform { scoped(it, config, route, nextSegment) }.saveAs("offNext")),
        ).exitHereIfFailed()

    private fun getForm(
        config: BasicRunConfig,
        name: String,
        route: String,
        expectedSegment: String,
        fieldSuffix: String?,
    ): ChainBuilder =
        exec(
            http(name)
                .get { session ->
                    val page = requireNotNull(session.getString("offNext"))
                    require(URI.create(page).path == "$route/$expectedSegment") {
                        "Offboarding journey reached an unexpected step: ${URI.create(page).path}"
                    }
                    page
                }.disableFollowRedirect()
                .check(status().`is`(200))
                .check(css("form[method=post]", "action").saveAs("offAction"))
                .check(css("form input[name=_csrf]", "value").exists())
                .check(css("form input[type=hidden]", "name").findAll().saveAs("offHiddenNames"))
                .check(css("form input[type=hidden]", "value").findAll().saveAs("offHiddenValues"))
                // Gatling builders are immutable, so the field check must be chained rather than applied.
                .let { request ->
                    if (fieldSuffix == null) request else request.check(css("form input[name$='$fieldSuffix']", "name").saveAs("offField"))
                },
        ).exitHereIfFailed()

    private fun submitForm(
        config: BasicRunConfig,
        name: String,
        route: String,
        fields: Map<String, String>,
        expectedNext: String,
    ): ChainBuilder =
        exec(
            http(name)
                .post { session ->
                    JointAgentJourneySupport.formAction(
                        requireNotNull(session.getString("offAction")),
                        config,
                        requireNotNull(session.getString("offNext")),
                    )
                }.disableFollowRedirect()
                .formParamMap { session ->
                    val names = session.getList<String>("offHiddenNames")
                    val values = session.getList<String>("offHiddenValues")
                    require(names.size == values.size) { "An offboarding form omitted a hidden field value" }
                    val mappedFields =
                        fields.map { (key, value) ->
                            if (key == "wantsToProceed") {
                                requireNotNull(session.getString("offField")) { "The offboarding form field was not captured" } to value
                            } else {
                                key to value
                            }
                        }
                    names.zip(values).toMap() + mappedFields
                }.check(status().`is`(302))
                .check(
                    header("Location")
                        .transform { location ->
                            val resolved = scoped(location, config, route, null)
                            require(URI.create(resolved).path == expectedNext) {
                                "Offboarding step redirected to an unexpected destination: ${URI.create(resolved).path}"
                            }
                            resolved
                        }.saveAs("offNext"),
                ),
        ).exitHereIfFailed()

    private fun scoped(
        location: String,
        config: BasicRunConfig,
        route: String,
        segment: String?,
    ): String {
        val resolved = HttpDestination.resolve(location, config.baseUrl)
        val path = URI.create(resolved).path
        require(path == route || path.startsWith("$route/")) { "Offboarding journey left its allocated route" }
        if (segment != null) require(path.endsWith("/$segment")) { "Offboarding journey reached an unexpected step" }
        return resolved
    }
}
