package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class AuthenticationContractSimulation : Simulation() {
    init {
        val config =
            BasicRunConfig.from(
                System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) },
            )
        setUp(
            scenario("Authentication contract")
                .exec(OneLoginAuthentication.chain(config))
                .exec(http("Mutation after login").post("/mutation").check(status().`is`(200)))
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(BasicPerformanceAssertions.forRequests(config, listOf("Mutation after login")))
    }
}
