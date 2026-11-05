package uk.gov.communities.prsdb.webapp.performance

import com.fasterxml.jackson.databind.ObjectMapper
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class OrganisationStructureSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val operation = properties["gatling.organisation.structureOperation"].orEmpty()
        require(operation in OrganisationStructureJourney.operations) {
            "gatling.organisation.structureOperation must be one of ${OrganisationStructureJourney.operations}"
        }
        val path =
            requireNotNull(properties["gatling.organisation.structureFixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.organisation.structureFixturePath must identify the read-only export from " +
                    "data-nft-organisation-structure-fixtures.sql"
            }
        val offset = properties["gatling.organisation.fixtureOffset"]?.toIntOrNull() ?: 0
        require(offset >= 0) { "gatling.organisation.fixtureOffset must not be negative" }
        val pool =
            requireNotNull(ObjectMapper().readTree(Files.readString(Path.of(path))).get(operation)) {
                "Structure fixture export has no $operation pool"
            }
        val fixtures = OrganisationFixture.parse(pool.toString(), offset + config.iterations + 1).drop(offset)
        val scenarios =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                scenario("Organisation structure $operation ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, LOGIN_PREFIX, prefix))
                    .exec(OrganisationStructureJourney.journey(config, fixture, operation, prefix))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(scenarios)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.loginRequestNames(LOGIN_PREFIX) + OrganisationStructureJourney.requestNames(operation),
                ),
            )
    }

    private companion object {
        const val LOGIN_PREFIX = "Organisation authentication"
    }
}
