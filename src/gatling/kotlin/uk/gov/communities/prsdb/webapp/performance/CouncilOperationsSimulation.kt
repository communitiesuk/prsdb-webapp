package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class CouncilOperationsSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        require(properties["gatling.council.emailDeliveryStub"] == "true") {
            "Council mutations require explicit confirmation that email delivery is stubbed: gatling.council.emailDeliveryStub=true"
        }
        val fixturePath =
            requireNotNull(properties["gatling.council.fixturePath"]) {
                "gatling.council.fixturePath must identify a fresh export from data-nft-council-fixtures.sql"
            }
        val fixtures = CouncilFixture.parse(Files.readString(Path.of(fixturePath)), config.iterations + 1)
        val populations =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                val base = "/local-council/${fixture.councilId}"
                scenario("Council operations ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(CouncilAuthentication.chain(config, fixture.subject, prefix))
                    .exec(CouncilReadJourney.chain(config, fixture, prefix))
                    .exec(
                        CouncilHttpForms.accessChange(
                            config,
                            "$base/edit-user/${fixture.targetId}",
                            "$base/manage-users",
                            fixture.targetName,
                            fixture.targetManager,
                            prefix,
                        ),
                    )
                    .exec(CouncilManagementJourney.invitation(config, fixture.councilId, false, prefix))
                    .exec(CouncilManagementJourney.accessRemoval(config, base, fixture.targetId, fixture.targetName, prefix))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(populations).protocols(
            http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"),
        ).assertions(
            BasicPerformanceAssertions.forRequests(
                config,
                listOf(
                    "Council: dashboard", "Council search: LRN", "Council search: PRN", "Council search: UPRN",
                    "Council search: landlord name", "Council search: property address",
                    "Council search: council and licence filters", "Council search: local landlords",
                    "Council search: pagination first page", "Council search: pagination next page",
                    "Council access 1: persisted role", "Council access 2: persisted role",
                    "Council invitation: confirmation", "Council invitation: persisted cancellation",
                    "Council access removal: persisted deletion",
                ),
            ),
        )
    }
}
