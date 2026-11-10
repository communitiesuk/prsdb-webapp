package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class RegistrationContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val fixture = RegistrationFixture("synthetic-landlord", "1 Fixture Road, FA1 1AA", "FA1 1AA", "1", 1, 901)
        setUp(
            scenario("Full registration HTTP contract").exec(
                RegistrationJourney.chain(
                    config,
                    fixture,
                    RegistrationVariant.VACANT_MANUAL_EXEMPT,
                    "${config.baseUrl}/local/gov-uk-pay",
                    null,
                ),
            ).injectOpen(atOnceUsers(1)),
        )
            .protocols(http.baseUrl(config.baseUrl))
            .assertions(BasicPerformanceAssertions.forRequests(config, RegistrationJourney.requiredRequests))
    }
}
