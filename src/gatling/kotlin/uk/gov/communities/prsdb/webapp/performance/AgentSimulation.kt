package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class AgentSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val fixturePath =
            requireNotNull(properties["gatling.agent.fixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.fixturePath must identify the read-only export from " +
                    "data-nft-agent-fixtures.sql or data-nft-agent-prepare-fixtures.sql"
            }
        val preparing =
            when (properties["gatling.agent.mode"]?.takeIf { it.isNotBlank() } ?: "delegate-remove") {
                "delegate-remove" -> false
                "prepare-delegation" -> true
                else -> throw IllegalArgumentException("gatling.agent.mode must be delegate-remove or prepare-delegation")
            }
        val families = AgentJourney.preparationFamilies
        val required = if (preparing) families.size * (config.iterations + 1) else config.iterations + 1
        val fixtures = AgentFixture.parse(Files.readString(Path.of(fixturePath)), required)
        require(fixtures.all { it.token == null }) {
            "Agent owner delegation fixtures must not contain existing access tokens"
        }
        val scenarios =
            fixtures.mapIndexed { index, fixture ->
                val family = if (preparing) families[index % families.size] else null
                val pass = if (preparing) index / families.size else index
                val familyPrefix = family?.let { AgentJourney.preparationPrefix(it) }.orEmpty()
                val warmUpPrefix = if (pass == 0) "Warm-up: " else ""
                val prefix = warmUpPrefix + familyPrefix
                val preparedEmail = family?.let { AgentJourney.preparationEmail(it, fixture.propertyOwnershipId) }
                scenario("Letting agent $familyPrefix${if (pass == 0) "warm-up" else "iteration $pass"} ${fixture.propertyOwnershipId}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.ownerSubject, loginPrefix(family), warmUpPrefix))
                    .exec(AgentJourney.ownerChain(config, fixture, prefix, preparedEmail))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(scenarios)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    if (preparing) {
                        families.flatMap { family ->
                            OneLoginAuthentication.loginRequestNames(loginPrefix(family)) +
                                AgentJourney.preparationRequestNames.map { AgentJourney.preparationPrefix(family) + it }
                        }
                    } else {
                        OneLoginAuthentication.loginRequestNames(LOGIN_PREFIX) + AgentJourney.ownerRequestNames
                    },
                ),
            )
    }

    companion object {
        private const val LOGIN_PREFIX = "Letting agent owner authentication"

        // The prefix becomes a Gatling session key in OneLoginAuthentication, so it must not contain EL-reserved characters.
        internal fun loginPrefix(family: String?) = family?.let { "$LOGIN_PREFIX $it preparation" } ?: LOGIN_PREFIX
    }
}
