package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

object CouncilRegistrationJourney {
    private const val ROUTE = "/local-council/register-local-council-user"

    fun chain(
        config: BasicRunConfig,
        fixture: CouncilRegistrationFixture,
        prefix: String = "",
    ): ChainBuilder {
        val name = "Gatling Council ${fixture.validToken.take(8)}"
        var chain =
            exec(
                http("${prefix}Council registration: expired invitation").get(ROUTE)
                    .queryParam("token", fixture.expiredToken).disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(
                        header("Location").transform { CouncilHttpForms.destination(it, config, "$ROUTE/invalid-link") }
                            .saveAs("councilNextUrl"),
                    ),
            ).exitHereIfFailed()
                .exec(
                    http("${prefix}Council registration: expired invitation explanation").get("#{councilNextUrl}")
                        .disableFollowRedirect().check(status().`is`(200))
                        .check(css("h1").`is`("There was a problem with this invitation link")),
                ).exitHereIfFailed()
                .exec(
                    http("${prefix}Council registration: valid invitation").get(ROUTE)
                        .queryParam("token", fixture.validToken).disableFollowRedirect()
                        .check(status().`is`(302))
                        .check(
                            header("Location").transform { CouncilHttpForms.destination(it, config, "$ROUTE/privacy-notice") }
                                .saveAs("councilNextUrl"),
                        ),
                ).exitHereIfFailed()
                .exec(
                    http("${prefix}Council registration: initialise journey").get("#{councilNextUrl}").disableFollowRedirect()
                        .check(status().`is`(302))
                        .check(
                            header("Location").transform {
                                val resolved = CouncilHttpForms.destination(it, config, "$ROUTE/privacy-notice")
                                CouncilHttpForms.journeyId(resolved)
                                resolved
                            }.saveAs("councilNextUrl"),
                        ),
                ).exitHereIfFailed()
                .exec { session -> session.set("councilJourneyId", CouncilHttpForms.journeyId(session.getString("councilNextUrl")!!)) }
        val steps =
            listOf(
                Step("privacy-notice", mapOf("agreesToPrivacyNotice" to "true"), "landing-page"),
                Step("landing-page", emptyMap(), "name"),
                Step("name", mapOf("name" to name), "email"),
                Step("email", mapOf("emailAddress" to fixture.email), "check-answers"),
                Step("check-answers", emptyMap(), "confirmation"),
            )
        for (step in steps) {
            chain =
                chain.exec(
                    CouncilHttpForms.form(
                        config, "${prefix}Council registration: ${step.route} form", "#{councilNextUrl}", "$ROUTE/${step.route}",
                        if (step.route == "check-answers") fixture.email else null,
                        correlateJourney = true,
                    ),
                ).exec(
                    CouncilHttpForms.submit(
                        config, "${prefix}Council registration: ${step.route} submit", step.fields, "$ROUTE/${step.next}",
                        correlateJourney = true,
                    ),
                )
        }
        return chain.exec(
            http("${prefix}Council registration: refreshed authentication required").get("#{councilNextUrl}").disableFollowRedirect()
                .check(status().`is`(302))
                .check(
                    header("Location").transform {
                        CouncilHttpForms.destination(it, config, "/oauth2/authorization/internal-access")
                    }.exists(),
                ),
        ).exitHereIfFailed()
            .exec(CouncilAuthentication.chain(config, fixture.subject, "${prefix}Registration refresh: "))
            .exec(
                http("${prefix}Council registration: confirmation").get("#{councilNextUrl}").disableFollowRedirect()
                    .check(status().`is`(200)).check(css("main").transform { it.contains(fixture.councilName) }.`is`(true)),
            ).exitHereIfFailed()
            .exec(
                http("${prefix}Council registration: persisted user").get("/local-council/dashboard").disableFollowRedirect()
                    .check(status().`is`(200)).check(css("h1").`is`(name))
                    .check(css("main").transform { it.contains(fixture.councilName) }.`is`(true)),
            ).exitHereIfFailed()
            .exec(
                http("${prefix}Council registration: invitation consumed").get(ROUTE).queryParam("token", fixture.validToken)
                    .disableFollowRedirect().check(status().`is`(302))
                    .check(
                        header("Location").transform { CouncilHttpForms.destination(it, config, "$ROUTE/invalid-link") }.exists(),
                    ),
            ).exitHereIfFailed()
    }

    private data class Step(val route: String, val fields: Map<String, String>, val next: String)
}
