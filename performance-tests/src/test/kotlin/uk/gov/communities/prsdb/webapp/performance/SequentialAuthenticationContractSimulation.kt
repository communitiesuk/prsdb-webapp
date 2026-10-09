package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class SequentialAuthenticationContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val registrationSubject =
            requireNotNull(properties["gatling.basic.registrationSubject"]) {
                "gatling.basic.registrationSubject must be provided for registration"
            }
        val phoneAuthentication =
            scenario("Seeded landlord authentication")
                .exec(OneLoginAuthentication.loginChain(config, config.landlordSubject, "Phone authentication"))
                .injectOpen(atOnceUsers(1))
        val registrationAuthentication =
            scenario("New registration authentication")
                .exec(OneLoginAuthentication.loginChain(config, registrationSubject, "Registration authentication"))
                .injectOpen(atOnceUsers(1))

        setUp(phoneAuthentication.andThen(registrationAuthentication))
            .protocols(http.baseUrl(config.baseUrl))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.loginRequestNames("Phone authentication") +
                        OneLoginAuthentication.loginRequestNames("Registration authentication"),
                ),
            )
    }
}
