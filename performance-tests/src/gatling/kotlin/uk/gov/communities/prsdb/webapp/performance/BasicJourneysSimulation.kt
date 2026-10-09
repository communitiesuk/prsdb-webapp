package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http

class BasicJourneysSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config =
            BasicRunConfig.from(properties)
        val registrationSubject =
            requireNotNull(properties["gatling.basic.registrationSubject"]?.takeIf { it.isNotBlank() }) {
                "gatling.basic.registrationSubject must be provided for the registration journey"
            }
        require(registrationSubject != config.landlordSubject) {
            "The registration subject must be distinct from the seeded phone/email-update landlord"
        }
        val phoneUpdate =
            scenario("Seeded landlord phone update")
                .exec(OneLoginAuthentication.chain(config))
                .exec(LandlordPhoneUpdateJourney.chain(config))
                .exec(LandlordEmailUpdateJourney.chain(config))
                .injectOpen(atOnceUsers(1))
        val registration =
            scenario("Fresh individual landlord registration")
                .exec(
                    OneLoginAuthentication.loginChain(
                        config,
                        registrationSubject,
                        REGISTRATION_LOGIN_PREFIX,
                    ),
                ).exec(IndividualLandlordRegistrationJourney.chain(config, registrationSubject))
                .injectOpen(atOnceUsers(1))

        setUp(
            phoneUpdate.andThen(registration),
        ).protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.applicationRequestNames +
                        LandlordPhoneUpdateJourney.requestNames +
                        LandlordEmailUpdateJourney.requestNames +
                        OneLoginAuthentication.loginRequestNames(REGISTRATION_LOGIN_PREFIX) +
                        IndividualLandlordRegistrationJourney.requestNames,
                ),
            )
    }

    companion object {
        private const val REGISTRATION_LOGIN_PREFIX = "Registration authentication"
    }
}
