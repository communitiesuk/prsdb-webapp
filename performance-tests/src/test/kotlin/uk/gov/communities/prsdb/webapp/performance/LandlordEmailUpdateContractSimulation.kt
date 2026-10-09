package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class LandlordEmailUpdateContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)

        setUp(
            scenario("Landlord email update contract")
                .exec { session ->
                    session.set(
                        "emailUpdateEntry",
                        "${config.baseUrl}/landlord/landlord-details/update-email/email",
                    )
                }.exec(LandlordEmailUpdateJourney.chain(config))
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(BasicPerformanceAssertions.forRequests(config, LandlordEmailUpdateJourney.requestNames))
    }
}
