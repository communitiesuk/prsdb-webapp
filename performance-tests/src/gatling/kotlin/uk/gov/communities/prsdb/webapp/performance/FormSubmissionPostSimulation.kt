package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

/**
 * Toy simulation establishing the reusable "GET a form, extract the CSRF token, then POST" pattern that future
 * simulations against real webapp journeys will need. Gatling's HTTP protocol automatically carries the session
 * cookie issued by the GET request into the following POST for the same virtual user.
 *
 * Targets the NFT-only PerformanceTestFormSubmissionController (PDJB-431), which is a no-op POST target rather
 * than a real journey step.
 */
class FormSubmissionPostSimulation : Simulation() {
    private val httpProtocol =
        http
            .baseUrl(BaseUrlConfig.baseUrl)
            .acceptHeader("text/html")

    private val formSubmissionScenario =
        scenario("GET then POST /performance-test/form-submission")
            .exec(
                http("Get form-submission page")
                    .get("/performance-test/form-submission")
                    .check(status().`is`(200))
                    .check(css("input[name='_csrf']", "value").saveAs("csrfToken")),
            ).exec(
                http("Submit form-submission")
                    .post("/performance-test/form-submission")
                    .formParam("_csrf", "#{csrfToken}")
                    .check(status().`is`(200)),
            )

    init {
        setUp(
            formSubmissionScenario.injectOpen(atOnceUsers(3)),
        ).protocols(httpProtocol)
    }
}
