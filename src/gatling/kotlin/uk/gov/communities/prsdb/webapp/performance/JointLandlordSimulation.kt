package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class JointLandlordSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val fixturePath =
            requireNotNull(properties["gatling.joint.fixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.joint.fixturePath must identify the read-only export from data-nft-joint-landlord-fixtures.sql"
            }
        val mode = properties["gatling.joint.mode"]?.takeIf { it.isNotBlank() } ?: "invite-resend-cancel"
        require(mode == "invite-resend-cancel" || mode == "prepare-decision") {
            "gatling.joint.mode must be invite-resend-cancel or prepare-decision"
        }
        val preparing = mode == "prepare-decision"
        val json = Files.readString(Path.of(fixturePath))
        val fixtures =
            if (preparing) {
                JointLandlordFixture.parsePreparations(json, config.iterations + 1)
            } else {
                JointLandlordFixture.parse(json, config.iterations + 1).also { fixtures ->
                    require(fixtures.all { it.invitedEmail == null }) {
                        "Invite-resend-cancel fixtures must not name an invitee; use gatling.joint.mode=prepare-decision"
                    }
                }
            }
        val requestNames = if (preparing) JointLandlordJourney.preparationRequestNames else JointLandlordJourney.requestNames
        val scenarios =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                scenario("Joint landlord ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.ownerSubject, LOGIN_PREFIX, prefix))
                    .exec(JointLandlordJourney.chain(config, fixture, prefix))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(scenarios)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.loginRequestNames(LOGIN_PREFIX) + requestNames,
                ),
            )
    }

    private companion object {
        const val LOGIN_PREFIX = "Joint landlord authentication"
    }
}
