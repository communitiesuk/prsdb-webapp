package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class IdentityVerificationContractSimulation : Simulation() {
    init {
        val config =
            BasicRunConfig.from(
                System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) },
            )
        val includeCoreIdentity = !java.lang.Boolean.getBoolean("gatling.contract.identityUnverified")
        val fixture = IdentityVerificationFixture.synthetic(includeCoreIdentity)

        setUp(
            scenario("Identity verification contract")
                .exec(
                    OneLoginAuthentication.identityVerificationChain(
                        config,
                        "/registration/verify-identity?journeyId=synthetic",
                        fixture,
                    ),
                ).injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.identityVerificationRequestNames,
                ),
            )
    }
}
