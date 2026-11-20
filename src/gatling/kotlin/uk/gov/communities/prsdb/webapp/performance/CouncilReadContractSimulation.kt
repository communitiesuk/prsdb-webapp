package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class CouncilReadContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val simulatorUrl =
            requireNotNull(properties["gatling.council.simulatorUrl"]?.takeIf(String::isNotBlank)) {
                "gatling.council.simulatorUrl must identify the separate internal-access simulator"
            }
        val mode = properties["gatling.council.mode"] ?: "read"
        val journey =
            when (mode) {
                "authentication" -> {
                    val subject =
                        requireNotNull(properties["gatling.council.subject"]?.takeIf(String::isNotBlank)) {
                            "gatling.council.subject is required for the authentication contract"
                        }
                    CouncilAuthentication.chain(config, subject, simulatorUrl)
                }

                "read" -> {
                    val fixturePath =
                        requireNotNull(properties["gatling.council.fixturePath"]?.takeIf(String::isNotBlank)) {
                            "gatling.council.fixturePath must identify a council fixture export"
                        }
                    val fixture = CouncilFixture.parse(Files.readString(Path.of(fixturePath)), 1).single()
                    CouncilAuthentication.chain(config, fixture.subject, simulatorUrl)
                        .exec(CouncilReadJourney.chain(config, fixture))
                }

                else -> throw IllegalArgumentException("gatling.council.mode must be authentication or read")
            }
        setUp(
            scenario("Council authentication and read contract").exec(journey).injectOpen(atOnceUsers(1)),
        ).protocols(
            http.baseUrl(config.councilBaseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"),
        ).assertions(global().failedRequests().count().`is`(0L))
    }
}
