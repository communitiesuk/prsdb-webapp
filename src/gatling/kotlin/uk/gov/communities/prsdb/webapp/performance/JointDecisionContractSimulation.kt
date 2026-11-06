package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

class JointDecisionContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val decision =
            requireNotNull(properties["gatling.joint.invitationDecision"]?.takeIf { it.isNotBlank() }) {
                "gatling.joint.invitationDecision must be accept, accept-new-user or reject"
            }
        require(decision in setOf("accept", "accept-new-user", "reject")) {
            "gatling.joint.invitationDecision must be accept, accept-new-user or reject"
        }
        val accept = decision != "reject"
        // The real embedded registration is covered by its own contracts; this stand-in proves the hand-off only.
        val registration =
            if (decision == "accept-new-user") {
                exec(
                    http("#{contractPrefix}Joint invitation: contract registration")
                        .post("#{${JointLandlordJourney.REGISTRATION_ENTRY_KEY}}")
                        .disableFollowRedirect()
                        .check(status().`is`(302))
                        .check(
                            header("Location")
                                .transform { HttpDestination.resolve(it, config.baseUrl) }
                                .saveAs("registrationNextUrl"),
                        ),
                )
            } else {
                null
            }
        val warmUp =
            JointLandlordFixture(
                "owner-warmup",
                101,
                "1 Fixture Road",
                201,
                "00000000-0000-0000-0000-000000000201",
                "invitee-warmup",
                "invitee-101@example.test",
            )
        val measured =
            JointLandlordFixture(
                "owner-measured",
                102,
                "2 Fixture Road",
                202,
                "00000000-0000-0000-0000-000000000202",
                "invitee-measured",
                "invitee-102@example.test",
            )
        setUp(
            scenario("Joint invitation decision HTTP contract")
                .exec { it.set("contractPrefix", "Warm-up: ") }
                .exec(JointLandlordJourney.decisionChain(config, warmUp, accept, "Warm-up: ", registration))
                .exec { it.set("contractPrefix", "") }
                .exec(JointLandlordJourney.decisionChain(config, measured, accept, registration = registration))
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(config, JointLandlordJourney.decisionRequestNames(accept, registration != null)),
            )
    }
}
