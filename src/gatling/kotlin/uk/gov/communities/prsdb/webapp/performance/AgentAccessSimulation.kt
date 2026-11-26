package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class AgentAccessSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val accessMode = properties["gatling.agent.accessMode"]?.takeIf { it.isNotBlank() } ?: "unclaimed"
        val hasPassword =
            when (accessMode) {
                "unclaimed" -> false
                "existing-password" -> true
                else -> error("Unknown letting-agent access mode: $accessMode")
            }
        val fixtureProperty = if (hasPassword) "gatling.agent.existingPasswordFixturePath" else "gatling.agent.accessFixturePath"
        val fixturePath =
            requireNotNull(properties[fixtureProperty]?.takeIf { it.isNotBlank() }) {
                "$fixtureProperty must identify the read-only letting-agent fixture export"
            }
        val password =
            requireNotNull(properties["gatling.agent.password"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.password must provide the password for letting-agent invitations"
            }
        val fixtures =
            AgentFixture.parseAccessFixtures(
                Files.readString(Path.of(fixturePath)),
                config.iterations + 1,
                hasPassword = hasPassword,
            )
        if (hasPassword) {
            val unclaimedFixturePath =
                requireNotNull(properties["gatling.agent.accessFixturePath"]?.takeIf { it.isNotBlank() }) {
                    "gatling.agent.accessFixturePath must identify the separate unclaimed-invitation fixture export"
                }
            require(Path.of(fixturePath).toAbsolutePath().normalize() != Path.of(unclaimedFixturePath).toAbsolutePath().normalize()) {
                "Existing-password and unclaimed letting-agent fixtures must use separate exports"
            }
            val unclaimedFixtures =
                AgentFixture.parseAccessFixtures(
                    Files.readString(Path.of(unclaimedFixturePath)),
                    config.iterations + 1,
                    hasPassword = false,
                )
            require(
                fixtures.none { existing ->
                    unclaimedFixtures.any { unclaimed ->
                        existing.ownerSubject == unclaimed.ownerSubject ||
                            existing.propertyOwnershipId == unclaimed.propertyOwnershipId ||
                            existing.token == unclaimed.token ||
                            existing.invitedEmail == unclaimed.invitedEmail
                    }
                },
            ) {
                "Existing-password letting-agent fixtures must not reuse unclaimed actors, properties, or invitations"
            }
        }
        val scenarios =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                scenario("Letting-agent access ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(AgentJourney.accessAndUpdateChain(config, fixture, password, prefix))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(scenarios)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(BasicPerformanceAssertions.forRequests(config, AgentJourney.accessRequestNames(hasPassword)))
    }
}
