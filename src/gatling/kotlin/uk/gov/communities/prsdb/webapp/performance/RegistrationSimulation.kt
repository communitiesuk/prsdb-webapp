package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class RegistrationSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        require(config.target == PerformanceTarget.LOCAL) { "Full registration currently requires controlled loopback dependencies" }
        val selected =
            properties["gatling.registration.variants"]?.split(',')?.map { RegistrationVariant.valueOf(it.trim()) }
                ?: RegistrationVariant.entries.filter { it.gas != "boundary" }
        require(selected.isNotEmpty() && selected.distinct().size == selected.size) { "Registration variants must be unique" }
        val fixturePath =
            requireNotNull(properties["gatling.registration.fixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.registration.fixturePath must identify the read-only export from data-nft-registration-fixtures.sql"
            }
        val offset = properties["gatling.registration.fixtureOffset"]?.toInt() ?: 0
        require(offset >= 0)
        val fixtures =
            RegistrationFixture.parse(
                Files.readString(Path.of(fixturePath)),
                offset + (config.iterations + 1) * selected.size,
            ).drop(offset)
        val certificatePath = properties["gatling.registration.certificatePath"]
        val gas14Months = properties["gatling.registration.gas14Months"]?.toBooleanStrict() ?: true
        val eicIssueDate = properties["gatling.registration.eicIssueDate"]?.toBooleanStrict() ?: true
        if (selected.any { it.gas == "upload" || (it.gas == "boundary" && gas14Months) || it.electrical.endsWith("upload") }) {
            require(certificatePath != null && Files.isRegularFile(Path.of(certificatePath))) {
                "A real local PDF certificate is required before starting upload variants"
            }
        }
        val paymentBase =
            PaymentJourney.localBaseUrl(
                properties["gatling.registration.paymentBaseUrl"] ?: "${config.baseUrl}/local/gov-uk-pay",
            )
        val populations =
            fixtures.mapIndexed { index, fixture ->
                val pass = index / selected.size
                val variant = selected[index % selected.size]
                val prefix = (if (pass == 0) "Warm-up: " else "") + "${variant.name}: "
                scenario("Registration ${variant.name} ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, "Registration authentication", prefix))
                    .exec(
                        RegistrationJourney.chain(
                            config,
                            fixture,
                            variant,
                            paymentBase,
                            certificatePath,
                            prefix,
                            gas14MonthExpiry = gas14Months,
                            eicIssueDate = eicIssueDate,
                        ),
                    )
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(populations)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    selected.flatMap { variant ->
                        (OneLoginAuthentication.loginRequestNames("Registration authentication") + RegistrationJourney.requiredRequests)
                            .map { "${variant.name}: $it" }
                    },
                ),
            )
    }
}
