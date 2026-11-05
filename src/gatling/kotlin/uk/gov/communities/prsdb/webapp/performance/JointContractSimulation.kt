package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class JointContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val preparing = properties["gatling.joint.mode"] == "prepare-decision"

        fun invitee(id: Long) = if (preparing) "invitee-$id" else null

        fun email(id: Long) = if (preparing) "invitee-$id@example.test" else null
        val warmUp = JointLandlordFixture("owner-warmup", 101, "1 Fixture Road", null, null, invitee(101), email(101))
        val measured = JointLandlordFixture("owner-measured", 102, "2 Fixture Road", null, null, invitee(102), email(102))
        val requestNames = if (preparing) JointLandlordJourney.preparationRequestNames else JointLandlordJourney.requestNames
        setUp(
            scenario("Joint landlord HTTP contract")
                .exec(JointLandlordJourney.chain(config, warmUp, "Warm-up: "))
                .exec(JointLandlordJourney.chain(config, measured))
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(BasicPerformanceAssertions.forRequests(config, requestNames))
    }
}
