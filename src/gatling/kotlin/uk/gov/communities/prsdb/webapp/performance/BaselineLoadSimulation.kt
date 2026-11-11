package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.details
import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.core.CoreDsl.nothingFor
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.PopulationBuilder
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

// Retains the previously exercised NFT workload, without the unfinished payment/EPC dependency strategy.
// Landlord registration is a provisional load proxy, not a property-registration capacity measurement.
class BaselineLoadSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties).copy(thinkTime = Duration.ofSeconds(2))
        val registrationSubject =
            requireNotNull(properties["gatling.basic.registrationSubject"]?.takeIf { it.isNotBlank() }) {
                "gatling.basic.registrationSubject must be provided for the registration journey"
            }
        val rampSeconds = decimal(properties, "gatling.ramp.rampMinutes", 5.0) * 60
        val holdSeconds = decimal(properties, "gatling.ramp.holdMinutes", 15.0) * 60
        val registrationTimes =
            RampSchedule(decimal(properties, "gatling.ramp.registrationsPerMinute", 26.0), rampSeconds, holdSeconds).arrivalTimesMillis()
        val updateTimes =
            RampSchedule(decimal(properties, "gatling.ramp.updatesPerMinute", 4.0), rampSeconds, holdSeconds).arrivalTimesMillis()
        val offset = properties["gatling.ramp.fixtureOffset"]?.toInt() ?: 22
        require(offset >= 22) { "Baseline ramp fixtures must skip the correctness-stage allocations" }

        fun fixtureJson(key: String) =
            Files.readString(
                Path.of(requireNotNull(properties[key]?.takeIf { it.isNotBlank() }) { "$key must identify a read-only fixture export" }),
            )

        val savedFixtures =
            SavedRegistrationFixture.parse(fixtureJson("gatling.saved.fixturePath"), offset + updateTimes.size)
                .drop(offset).take(updateTimes.size)
        val propertyFixtures =
            CompletedPropertyFixture.parse(fixtureJson("gatling.property.fixturePath"), offset + updateTimes.size)
                .drop(offset).take(updateTimes.size)
        val organisationFixtures =
            OrganisationFixture.parse(fixtureJson("gatling.organisation.fixturePath"), offset + updateTimes.size)
                .drop(offset).take(updateTimes.size)
        val subjects = savedFixtures.map { it.subject } + propertyFixtures.map { it.subject } + organisationFixtures.map { it.subject }
        require(subjects.toSet().size == subjects.size) { "Baseline workload families share a mutable actor" }

        val registrations =
            registrationTimes.mapIndexed { index, at ->
                val data = BasicIterationData.forPass(registrationSubject, index + 1)
                require(data.registrationSubject !in subjects) { "Registration shares an existing mutable actor" }
                scenario("Baseline landlord registration ${index + 1}")
                    .exec(OneLoginAuthentication.loginChain(config, data.registrationSubject, "Registration authentication", ""))
                    .exec(IndividualLandlordRegistrationJourney.chain(config, data))
                    .arrive(at)
            }
        val saved =
            savedFixtures.zip(updateTimes) { fixture, at ->
                scenario("Baseline saved registration ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, "Saved registration authentication", ""))
                    .exec(SavedRegistrationJourney.chain(config, fixture, ""))
                    .arrive(at)
            }
        val completed =
            propertyFixtures.zip(updateTimes) { fixture, at ->
                scenario("Baseline completed property ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, "Completed property authentication", ""))
                    .exec(PropertyBranchJourney.chain(config, fixture, ""))
                    .exec(CompletedPropertyUpdateJourney.chain(config, fixture, ""))
                    .arrive(at)
            }
        val organisations =
            organisationFixtures.zip(updateTimes) { fixture, at ->
                scenario("Baseline organisation profile ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, "Organisation authentication", ""))
                    .exec(OrganisationProfileJourney.chain(config, fixture, ""))
                    .arrive(at)
            }
        setUp(registrations + saved + completed + organisations)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                global().failedRequests().percent().`is`(0.0),
                details(IndividualLandlordRegistrationJourney.DASHBOARD_REQUEST)
                    .successfulRequests().count().`is`(registrationTimes.size.toLong()),
                details(SavedRegistrationJourney.requestNames.last()).successfulRequests().count().`is`(updateTimes.size.toLong()),
                details(CompletedPropertyUpdateJourney.requestNames.last()).successfulRequests().count().`is`(updateTimes.size.toLong()),
                details(OrganisationProfileJourney.requestNames.last()).successfulRequests().count().`is`(updateTimes.size.toLong()),
            )
    }

    private fun ScenarioBuilder.arrive(millis: Long): PopulationBuilder = injectOpen(nothingFor(Duration.ofMillis(millis)), atOnceUsers(1))

    private fun decimal(
        properties: Map<String, String>,
        key: String,
        default: Double,
    ): Double = properties[key]?.toDouble() ?: default
}
