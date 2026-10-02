package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class BasicJourneysSimulation : Simulation() {
    init {
        val config =
            BasicRunConfig.from(
                System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) },
            )
        setUp(
            scenario("Basic landlord journeys")
                .exec(OneLoginAuthentication.chain(config))
                .exec(LandlordPhoneUpdateJourney.chain(config))
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.applicationRequestNames + LandlordPhoneUpdateJourney.requestNames,
                ),
            )
    }
}
