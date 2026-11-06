package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class IdentityVerificationContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)

        setUp(
            scenario("Unverified identity verification HTTP contract")
                .exec(
                    OneLoginAuthentication.identityVerificationChain(
                        config,
                        "${config.baseUrl}/id-verification/oauth2/authorize/one-login",
                        fixture = IdentityVerificationFixture.synthetic(includeCoreIdentity = false),
                        identitySubject = config.landlordSubject,
                    ),
                ).injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl))
            .assertions(
                BasicPerformanceAssertions.forRequests(config, OneLoginAuthentication.identityVerificationRequestNames),
            )
    }
}
