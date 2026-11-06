package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class OffboardingContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val actionName =
            requireNotNull(properties["gatling.offboarding.contract.action"]?.takeIf { it.isNotBlank() }) {
                "gatling.offboarding.contract.action must identify an offboarding action"
            }
        val action =
            OffboardingAction.entries.singleOrNull { it.value == actionName }
                ?: throw IllegalArgumentException("Unsupported offboarding contract action: $actionName")
        val fixture =
            OffboardingFixture(
                action = action,
                subject = "contract-landlord",
                propertyOwnershipId = if (action == OffboardingAction.DEREGISTER_LANDLORD) null else 101,
                address = if (action == OffboardingAction.DEREGISTER_LANDLORD) null else "1 Fixture Road, Testtown, AB1 2CD",
            )
        setUp(
            scenario("Offboarding $actionName HTTP contract")
                .exec(OffboardingJourney.chain(config, fixture))
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(BasicPerformanceAssertions.forRequests(config, OffboardingJourney.requestNames(action)))
    }
}
