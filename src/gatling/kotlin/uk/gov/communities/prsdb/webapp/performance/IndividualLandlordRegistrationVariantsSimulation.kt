package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class IndividualLandlordRegistrationVariantsSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val registrationSubject =
            requireNotNull(properties["gatling.basic.registrationSubject"]?.takeIf { it.isNotBlank() }) {
                "gatling.basic.registrationSubject must be provided for the registration journey"
            }
        require(registrationSubject != config.landlordSubject) {
            "The registration subject must be distinct from the seeded phone/email-update landlord"
        }

        val options =
            IndividualLandlordRegistrationJourney.Options(
                includeCoreIdentity = false,
                manualAddress = false,
                requestNamePrefix = "Registration selected address, unverified identity: ",
            )
        val loginRequestPrefix = "Selected-address registration authentication"
        val passes =
            listOf(BasicIterationData.forPass(registrationSubject, null)) +
                (1..config.iterations).map { BasicIterationData.forPass(registrationSubject, it) }
        val registrations =
            passes.mapIndexed { pass, data ->
                val variantData =
                    data.copy(
                        registrationSubject = "${data.registrationSubject}-selected-address-unverified",
                        registrationEmailAddress = data.registrationEmailAddress.replace("@", "-selected-address-unverified@"),
                    )
                scenario("Individual registration with selected address and unverified identity (${passName(pass)})")
                    .exec(
                        OneLoginAuthentication.loginChain(
                            config,
                            variantData.registrationSubject,
                            loginRequestPrefix,
                            variantData.requestNamePrefix,
                        ),
                    ).exec(IndividualLandlordRegistrationJourney.chain(config, variantData, options))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }

        setUp(registrations)
            .protocols(
                http
                    .baseUrl(config.baseUrl)
                    .acceptHeader("text/html")
                    .userAgentHeader("PRSDB-Gatling-Performance-Tests"),
            ).assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.loginRequestNames(loginRequestPrefix) +
                        IndividualLandlordRegistrationJourney.requestNames(options),
                ),
            )
    }

    companion object {
        private fun passName(pass: Int) = if (pass == 0) "warm-up" else "iteration $pass"
    }
}
