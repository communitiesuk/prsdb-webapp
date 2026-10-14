package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

/**
 * Toy simulation establishing the basic pattern for a Gatling Kotlin-DSL test: httpProtocol targeting
 * BaseUrlConfig.baseUrl, a single scenario/exec/check, and an atOnceUsers injection profile.
 *
 * Targets an existing public GET endpoint so no new webapp code is needed to prove the pattern.
 */
class RegisterAsLandlordGetSimulation : Simulation() {
    private val httpProtocol =
        http
            .baseUrl(BaseUrlConfig.baseUrl)
            .acceptHeader("text/html")

    private val registerAsLandlordScenario =
        scenario("GET /landlord/register-as-a-landlord")
            .exec(
                http("Get register-as-a-landlord page")
                    .get("/landlord/register-as-a-landlord")
                    .check(status().`is`(200)),
            )

    init {
        setUp(
            registerAsLandlordScenario.injectOpen(atOnceUsers(3)),
        ).protocols(httpProtocol)
    }
}
