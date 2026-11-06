package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class CouncilRegistrationSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config =
            BasicRunConfig.from(properties).let {
                properties["gatling.council.simulatorUrl"]?.let { simulator ->
                    BasicRunConfig.from(properties + ("gatling.simulatorUrl" to simulator))
                } ?: it
            }
        val path =
            requireNotNull(properties["gatling.council.registrationFixturePath"]) {
                "gatling.council.registrationFixturePath must identify a fresh export from data-nft-council-registration-fixtures.sql"
            }
        val fixtures = CouncilRegistrationFixture.parse(Files.readString(Path.of(path)), config.iterations + 1)
        val populations =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                scenario("Council registration ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(CouncilAuthentication.chain(config, fixture.subject, prefix))
                    .exec(CouncilRegistrationJourney.chain(config, fixture, prefix))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(populations).protocols(
            http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"),
        ).assertions(
            BasicPerformanceAssertions.forRequests(
                config,
                listOf(
                    "Council registration: expired invitation",
                    "Council registration: valid invitation",
                    "Council registration: confirmation",
                    "Council registration: persisted user",
                    "Council registration: invitation consumed",
                ),
            ),
        )
    }
}
