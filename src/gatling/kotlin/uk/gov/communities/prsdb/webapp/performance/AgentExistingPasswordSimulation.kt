package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.flushCookieJar
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class AgentExistingPasswordSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val fixturePath =
            requireNotNull(properties["gatling.agent.loginFixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.loginFixturePath must identify the read-only letting-agent access fixture export"
            }
        val password =
            requireNotNull(properties["gatling.agent.password"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.password must be a nonblank disposable letting-agent access password"
            }
        val fixtures =
            AgentFixture.parseAccessFixtures(
                Files.readString(Path.of(fixturePath)),
                config.iterations + 1,
                hasPassword = false,
            )
        val scenarios =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                scenario("Letting-agent existing-password login ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(
                        AgentJourney.accessAndUpdateChain(
                            config,
                            fixture,
                            password,
                            "$prefix$FIRST_TIME_PREFIX",
                        ),
                    )
                    .exec(flushCookieJar())
                    .exec(
                        AgentJourney.accessAndUpdateChain(
                            config,
                            fixture.copy(hasPassword = true),
                            password,
                            "$prefix$EXISTING_PASSWORD_PREFIX",
                        ),
                    )
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(scenarios)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    AgentJourney.accessRequestNames(false).map { "$FIRST_TIME_PREFIX$it" } +
                        AgentJourney.accessRequestNames(true).map { "$EXISTING_PASSWORD_PREFIX$it" },
                ),
            )
    }

    private companion object {
        const val FIRST_TIME_PREFIX = "First-time password: "
        const val EXISTING_PASSWORD_PREFIX = "Existing password: "
    }
}
