package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class CouncilContractSimulation : Simulation() {
    init {
        val config =
            BasicRunConfig.from(
                System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) },
            )
        val journey =
            if (System.getProperty("gatling.contract.council") == "authentication") {
                CouncilAuthentication.chain(config, config.landlordSubject)
            } else if (System.getProperty("gatling.contract.council") == "invitation") {
                CouncilManagementJourney.invitation(config, 2, false)
            } else {
                CouncilHttpForms.accessChange(
                    config,
                    "/local-council/2/edit-user/42",
                    "/local-council/2/manage-users",
                    "Fixture Council User",
                    false,
                )
            }
        setUp(
            scenario("Council HTTP contract").exec(journey).injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl)).assertions(global().failedRequests().count().`is`(0L))
    }
}
