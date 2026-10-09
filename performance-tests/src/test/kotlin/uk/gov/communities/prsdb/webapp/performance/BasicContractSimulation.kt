package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.bodyString
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class BasicContractSimulation : Simulation() {
    init {
        val config =
            BasicRunConfig.from(
                System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) },
            )
        setUp(
            scenario("HTTP contract")
                .exec(
                    http("Get form")
                        .get("/form")
                        .check(status().`is`(200))
                        .check(css("input[name='_csrf']", "value").saveAs("csrfToken"))
                        .check(css("form", "action").saveAs("formAction")),
                ).exitHereIfFailed()
                .doIf { !java.lang.Boolean.getBoolean("gatling.contract.skipSubmit") }.then(
                    http("Submit form")
                        .post("#{formAction}")
                        .formParam("_csrf", "#{csrfToken}")
                        .formParam("value", "value + & spaces")
                        .check(status().`is`(200))
                        .check(header("Content-Type").`is`("text/plain"))
                        .check(bodyString().`is`("Submission received (no-op)")),
                )
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(BasicPerformanceAssertions.forRequests(config, listOf("Get form", "Submit form")))
    }
}
