package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class ComplianceUpdateContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val certificatePath = requireNotNull(properties["gatling.contract.certificatePath"])
        val fixture = RegistrationFixture("synthetic-landlord", "1 Fixture Road, FA1 1AA", "FA1 1AA", "1", 1, 901)
        val electricalVariant =
            properties["gatling.contract.electricalVariant"]?.let(RegistrationVariant::valueOf)
                ?: ComplianceUpdateJourney.updatedVariant
        val chain =
            ComplianceUpdateKind.entries.fold(
                exec { session -> session.set("regPropertyUrl", "${config.baseUrl}/landlord/property-details/100?withBackUrl=17") },
            ) { chain, kind ->
                chain.exec(
                    ComplianceUpdateJourney.chain(
                        config,
                        fixture,
                        kind,
                        certificatePath,
                        electricalVariant = electricalVariant,
                        expectSuccessBanner = properties["gatling.contract.expectSuccessBanner"].toBoolean(),
                    ),
                )
            }
                .exec(ComplianceUpdateJourney.verifyAll(config, fixture, gas14MonthExpiry = true, electricalVariant = electricalVariant))
        setUp(scenario("Compliance update HTTP contract").exec(chain).injectOpen(atOnceUsers(1)))
            .protocols(http.baseUrl(config.baseUrl))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    ComplianceUpdateKind.entries.flatMap { ComplianceUpdateJourney.requestNames(it) } +
                        ComplianceUpdateJourney.finalRequest,
                ),
            )
    }
}
