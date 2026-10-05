package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class PhoneUpdateContractSimulation : Simulation() {
    init {
        val config =
            BasicRunConfig.from(
                System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) },
            )
        setUp(
            scenario("Phone update contract")
                .exec { session ->
                    session.set("phoneUpdateEntry", "/landlord/landlord-details/update-phone-number/phone-number")
                }.exec(LandlordPhoneUpdateJourney.chain(config))
            .exec(LandlordEmailUpdateJourney.chain(config))
            .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(
            BasicPerformanceAssertions.forRequests(
                config,
                LandlordPhoneUpdateJourney.requestNames + LandlordEmailUpdateJourney.requestNames,
            ),
            )
    }
}
