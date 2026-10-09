package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class IndividualLandlordRegistrationContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val registrationSubject =
            requireNotNull(properties["gatling.basic.registrationSubject"]) {
                "gatling.basic.registrationSubject must be provided for registration"
            }

        setUp(
            scenario("Individual landlord registration")
                .exec(IndividualLandlordRegistrationJourney.chain(config, registrationSubject))
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(BasicPerformanceAssertions.forRequests(config, IndividualLandlordRegistrationJourney.requestNames))
    }
}
