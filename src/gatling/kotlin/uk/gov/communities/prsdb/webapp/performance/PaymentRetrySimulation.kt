package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class PaymentRetrySimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        require(config.target == PerformanceTarget.LOCAL)
        val fixturePath =
            requireNotNull(properties["gatling.payment.fixturePath"]) {
                "gatling.payment.fixturePath must identify the separate data-nft-registration-payment-fixtures.sql export"
            }
        val offset = properties["gatling.payment.fixtureOffset"]?.toInt() ?: 0
        require(offset >= 0)
        val fixtures = RegistrationFixture.parse(Files.readString(Path.of(fixturePath)), offset + config.iterations + 1).drop(offset)
        val paymentBase = PaymentJourney.localBaseUrl(requireNotNull(properties["gatling.registration.paymentBaseUrl"]))
        val pending = properties["gatling.payment.pending"]?.toBooleanStrict() ?: false
        val populations =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                scenario("Registration failed-payment retry ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, "Payment retry authentication", prefix))
                    .exec(
                        RegistrationJourney.chain(
                            config,
                            fixture,
                            RegistrationVariant.VACANT_MANUAL_EXEMPT,
                            paymentBase,
                            null,
                            prefix,
                            expectPaymentRetry = !pending,
                            expectPaymentPending = pending,
                        ),
                    )
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(populations).protocols(http.baseUrl(config.baseUrl))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.loginRequestNames("Payment retry authentication") + RegistrationJourney.requiredRequests +
                        if (pending) "Registration: pending payment did not register" else "Registration: failed payment did not register",
                ),
            )
    }
}
