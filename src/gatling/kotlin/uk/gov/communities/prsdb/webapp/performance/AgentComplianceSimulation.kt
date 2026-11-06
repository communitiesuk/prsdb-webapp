package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentPropertyDetailsController
import java.nio.file.Files
import java.nio.file.Path

class AgentComplianceSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val fixturePath =
            requireNotNull(properties["gatling.agent.complianceFixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.complianceFixturePath must identify the read-only export from data-nft-agent-compliance-fixtures.sql"
            }
        val certificatePath =
            requireNotNull(properties["gatling.agent.certificatePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.certificatePath must identify a safe synthetic PDF"
            }
        require(Files.isRegularFile(Path.of(certificatePath))) { "The letting-agent certificate PDF does not exist" }
        val password =
            requireNotNull(properties["gatling.agent.password"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.password must be a nonblank disposable letting-agent access password"
            }
        val kindName =
            requireNotNull(properties["gatling.agent.complianceType"]?.takeIf { it.isNotBlank() }) {
                "gatling.agent.complianceType must be gas, eic or eicr"
            }
        val (kind, electricalVariant) =
            mapOf(
                "gas" to (ComplianceUpdateKind.GAS to ComplianceUpdateJourney.updatedVariant),
                "eic" to (ComplianceUpdateKind.ELECTRICAL to RegistrationVariant.OCCUPIED_MANUAL_SUPERSEDED),
                "eicr" to (ComplianceUpdateKind.ELECTRICAL to ComplianceUpdateJourney.updatedVariant),
            )[kindName] ?: throw IllegalArgumentException("gatling.agent.complianceType must be gas, eic or eicr")
        val fixtures =
            AgentFixture.parseAccessFixtures(
                Files.readString(Path.of(fixturePath)),
                config.iterations + 1,
                hasPassword = false,
            )
        val scenarios =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                val detailsUrl =
                    config.baseUrl +
                        LettingAgentPropertyDetailsController.LETTING_AGENT_PROPERTY_DETAILS_ROUTE.replace("{token}", fixture.token!!)
                scenario("Letting-agent $kindName compliance ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(AgentJourney.accessAndUpdateChain(config, fixture, password, prefix))
                    // The shared update chain reads the property it updates from this session key.
                    .exec { session -> session.set("regPropertyUrl", detailsUrl) }
                    .exec(
                        ComplianceUpdateJourney.chain(
                            config,
                            null,
                            kind,
                            certificatePath,
                            "$prefix$REQUEST_PREFIX",
                            propertyPathPattern = AGENT_PROPERTY_PATH,
                            verifySafeDownload = false,
                            electricalVariant = electricalVariant,
                            expectSuccessBanner = true,
                        ),
                    )
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(scenarios)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    AgentJourney.accessRequestNames(false) + ComplianceUpdateJourney.requestNames(kind).map { "$REQUEST_PREFIX$it" },
                ),
            )
    }

    private companion object {
        const val REQUEST_PREFIX = "Letting agent: "
        val AGENT_PROPERTY_PATH = Regex("/landlord/letting-agent/property-details/[0-9a-f-]{36}")
    }
}
