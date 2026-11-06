package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class ComplianceUpdateSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        require(config.target == PerformanceTarget.LOCAL) { "Compliance updates currently require controlled loopback dependencies" }
        val fixturePath =
            requireNotNull(properties["gatling.registration.fixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.registration.fixturePath must identify the read-only export from data-nft-registration-fixtures.sql"
            }
        val offset = properties["gatling.registration.fixtureOffset"]?.toInt() ?: 0
        require(offset >= 0)
        val certificatePath =
            requireNotNull(properties["gatling.registration.certificatePath"]?.takeIf { Files.isRegularFile(Path.of(it)) }) {
                "A real local PDF certificate is required for compliance update uploads"
            }
        val gas14Months = properties["gatling.registration.gas14Months"]?.toBooleanStrict() ?: true
        val eicIssueDate = properties["gatling.registration.eicIssueDate"]?.toBooleanStrict() ?: true
        val fixtures = RegistrationFixture.parse(Files.readString(Path.of(fixturePath)), offset + config.iterations + 1).drop(offset)
        val paymentBase =
            PaymentJourney.localBaseUrl(
                properties["gatling.registration.paymentBaseUrl"] ?: "${config.baseUrl}/local/gov-uk-pay",
            )
        val populations =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                ComplianceUpdateKind.entries.fold(
                    scenario("Compliance update ${if (pass == 0) "warm-up" else "iteration $pass"}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, "Compliance update authentication", prefix))
                        .exec(
                            RegistrationJourney.chain(
                                config,
                                fixture,
                                ComplianceUpdateJourney.registeredVariant,
                                paymentBase,
                                null,
                                prefix,
                                gas14MonthExpiry = gas14Months,
                                eicIssueDate = eicIssueDate,
                            ),
                        ),
                ) { scenario, kind -> scenario.exec(ComplianceUpdateJourney.chain(config, fixture, kind, certificatePath, prefix)) }
                    .exec(ComplianceUpdateJourney.verifyAll(config, fixture, gas14Months, prefix))
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(populations)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.loginRequestNames("Compliance update authentication") +
                        RegistrationJourney.requiredRequests +
                        ComplianceUpdateKind.entries.flatMap { ComplianceUpdateJourney.requestNames(it) } +
                        ComplianceUpdateJourney.finalRequest,
                ),
            )
    }
}
