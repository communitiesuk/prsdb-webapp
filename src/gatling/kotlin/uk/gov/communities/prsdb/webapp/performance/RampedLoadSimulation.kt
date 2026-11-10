package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.details
import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.core.CoreDsl.nothingFor
import io.gatling.javaapi.core.CoreDsl.pause
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.PopulationBuilder
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

// Ramped concurrent load across 11 representative NFT journey families, with an exclusive fixture per arrival.
class RampedLoadSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val registrationSubject =
            requireNotNull(properties["gatling.basic.registrationSubject"]?.takeIf { it.isNotBlank() }) {
                "gatling.basic.registrationSubject must be provided for the registration journey"
            }
        val rampSeconds = decimal(properties, "gatling.ramp.rampMinutes", 5.0) * SECONDS_PER_MINUTE
        val holdSeconds = decimal(properties, "gatling.ramp.holdMinutes", 15.0) * SECONDS_PER_MINUTE
        val schedules = RampedLoadProfile.schedules(rampSeconds, holdSeconds)
        val individualTimes = schedules.getValue(RampedJourneyFamily.INDIVIDUAL_LANDLORD).arrivalTimesMillis()
        val registrationTimes = schedules.getValue(RampedJourneyFamily.PROPERTY_REGISTRATION).arrivalTimesMillis()
        val savedTimes = schedules.getValue(RampedJourneyFamily.SAVED_PROPERTY).arrivalTimesMillis()
        val completedTimes = schedules.getValue(RampedJourneyFamily.COMPLETED_PROPERTY).arrivalTimesMillis()
        val complianceTimes = schedules.getValue(RampedJourneyFamily.CERTIFICATES_AND_COMPLIANCE).arrivalTimesMillis()
        val organisationTimes = schedules.getValue(RampedJourneyFamily.ORGANISATION).arrivalTimesMillis()
        val jointTimes = schedules.getValue(RampedJourneyFamily.JOINT_LANDLORD).arrivalTimesMillis()
        val agentTimes = schedules.getValue(RampedJourneyFamily.LETTING_AGENT).arrivalTimesMillis()
        val councilTimes = schedules.getValue(RampedJourneyFamily.LOCAL_COUNCIL).arrivalTimesMillis()
        val operatorTimes = schedules.getValue(RampedJourneyFamily.SYSTEM_OPERATOR).arrivalTimesMillis()
        val offboardingTimes = schedules.getValue(RampedJourneyFamily.OFFBOARDING).arrivalTimesMillis()
        require(properties["gatling.council.emailDeliveryStub"] == "true") {
            "Council and operator invitations require gatling.council.emailDeliveryStub=true"
        }
        val certificatePath =
            requireNotNull(properties["gatling.registration.certificatePath"]?.takeIf { Files.isRegularFile(Path.of(it)) }) {
                "A synthetic PDF certificate is required for ramped compliance uploads"
            }
        // Skips fixtures already used by the single-user simulations earlier in the same run.
        val offset = properties["gatling.ramp.fixtureOffset"]?.toInt() ?: 22
        require(offset >= 0)
        val operatorOffset = properties["gatling.ramp.operatorFixtureOffset"]?.toInt() ?: config.iterations + 1
        require(operatorOffset >= config.iterations + 1) { "Operator ramp fixtures must skip the correctness-stage removal targets" }

        fun fixtureJson(key: String) =
            Files.readString(
                Path.of(requireNotNull(properties[key]?.takeIf { it.isNotBlank() }) { "$key must identify a read-only fixture export" }),
            )

        val paymentBaseUrl =
            PaymentJourney.baseUrl(
                properties["gatling.registration.paymentBaseUrl"]
                    ?: run {
                        require(config.target == PerformanceTarget.LOCAL) {
                            "gatling.registration.paymentBaseUrl must explicitly configure the controlled NFT payment mock"
                        }
                        "${config.baseUrl}/local/gov-uk-pay"
                    },
                config.target,
                config.baseUrl,
            )
        val registrationFixtures = RegistrationFixture.parse(fixtureJson("gatling.registration.fixturePath"), registrationTimes.size)
        val individuals =
            individualTimes.mapIndexed { index, at ->
                val data = BasicIterationData.forPass(registrationSubject, index + 1)
                val options = IndividualLandlordRegistrationJourney.Options(requestNamePrefix = INDIVIDUAL_PREFIX)
                scenario("Ramped individual landlord registration ${index + 1}")
                    .exec(OneLoginAuthentication.loginChain(config, data.registrationSubject, INDIVIDUAL_LOGIN_PREFIX, ""))
                    .exec(pause(THINK_TIME))
                    .exec(IndividualLandlordRegistrationJourney.chain(config, data, options))
                    .arrive(at)
            }
        val propertyRegistrations =
            registrationFixtures.zip(registrationTimes) { fixture, at ->
                scenario("Ramped property registration ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, PROPERTY_REGISTRATION_LOGIN_PREFIX, ""))
                    .exec(pause(THINK_TIME))
                    .exec(
                        RegistrationJourney.chain(
                            config,
                            fixture,
                            RegistrationVariant.VACANT_MANUAL_EXEMPT,
                            paymentBaseUrl,
                            certificatePath = null,
                            prefix = PROPERTY_REGISTRATION_PREFIX,
                        ),
                    ).arrive(at)
            }
        val saved =
            SavedRegistrationFixture.parse(fixtureJson("gatling.saved.fixturePath"), offset + savedTimes.size).drop(offset)
                .zip(savedTimes) { fixture, at ->
                    scenario("Ramped saved registration ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, SAVED_LOGIN_PREFIX, ""))
                        .exec(pause(THINK_TIME))
                        .exec(SavedRegistrationJourney.chain(config, fixture, ""))
                        .arrive(at)
                }
        val completedPropertyFixtures =
            CompletedPropertyFixture.parse(
                fixtureJson("gatling.property.fixturePath"),
                offset + completedTimes.size + complianceTimes.size,
            ).drop(offset)
        val completed =
            completedPropertyFixtures.take(completedTimes.size).zip(completedTimes) { fixture, at ->
                    scenario("Ramped completed property ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, PROPERTY_LOGIN_PREFIX, ""))
                        .exec(pause(THINK_TIME))
                        .exec(PropertyBranchJourney.chain(config, fixture, ""))
                        .exec(pause(THINK_TIME))
                        .exec(CompletedPropertyUpdateJourney.chain(config, fixture, ""))
                        .arrive(at)
                }
        val compliance =
            completedPropertyFixtures.drop(completedTimes.size).take(complianceTimes.size).zip(complianceTimes) { fixture, at ->
                scenario("Ramped certificates and compliance ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, COMPLIANCE_LOGIN_PREFIX, ""))
                    .exec(pause(THINK_TIME))
                    .exec(ComplianceUpdateJourney.completedPropertyChain(config, fixture, certificatePath))
                    .arrive(at)
            }
        val organisations =
            OrganisationFixture.parse(fixtureJson("gatling.organisation.fixturePath"), offset + organisationTimes.size).drop(offset)
                .zip(organisationTimes) { fixture, at ->
                    scenario("Ramped organisation profile ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, ORGANISATION_LOGIN_PREFIX, ""))
                        .exec(pause(THINK_TIME))
                        .exec(OrganisationProfileJourney.chain(config, fixture, ""))
                        .arrive(at)
                }

        val operators =
            OperatorFixture.parse(fixtureJson("gatling.operator.fixturePath"), operatorOffset + operatorTimes.size).drop(operatorOffset)
                .zip(operatorTimes) { fixture, at ->
                    scenario("Ramped operator ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, OPERATOR_LOGIN_PREFIX, ""))
                        .exec(pause(THINK_TIME))
                        .exec(OperatorJourney.operations(config, fixture, OPERATOR_PREFIX))
                        .arrive(at)
                }

        val councils =
            CouncilFixture.parse(fixtureJson("gatling.council.fixturePath"), councilTimes.size).zip(councilTimes) { fixture, at ->
                val base = "/local-council/${fixture.councilId}"
                scenario("Ramped local council ${fixture.subject}")
                    .exec(CouncilAuthentication.chain(config, fixture.subject, COUNCIL_PREFIX))
                    .exec(pause(THINK_TIME))
                    .exec(CouncilReadJourney.chain(config, fixture, COUNCIL_PREFIX))
                    .exec(
                        CouncilHttpForms.accessChange(
                            config,
                            "$base/edit-user/${fixture.targetId}",
                            "$base/manage-users",
                            fixture.targetName,
                            fixture.targetManager,
                            COUNCIL_PREFIX,
                        ),
                    ).exec(CouncilManagementJourney.invitation(config, fixture.councilId, false, COUNCIL_PREFIX))
                    .exec(CouncilManagementJourney.accessRemoval(config, base, fixture.targetId, fixture.targetName, COUNCIL_PREFIX))
                    .arrive(at)
            }
        val joints =
            JointLandlordFixture.parse(fixtureJson("gatling.joint.fixturePath"), jointTimes.size).zip(jointTimes) { fixture, at ->
                scenario("Ramped joint landlord ${fixture.ownerSubject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.ownerSubject, JOINT_LOGIN_PREFIX, ""))
                    .exec(pause(THINK_TIME))
                    .exec(JointLandlordJourney.chain(config, fixture, JOINT_PREFIX))
                    .arrive(at)
            }
        val agents =
            AgentFixture.parse(fixtureJson("gatling.agent.fixturePath"), agentTimes.size).zip(agentTimes) { fixture, at ->
                scenario("Ramped letting agent ${fixture.ownerSubject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.ownerSubject, AGENT_LOGIN_PREFIX, ""))
                    .exec(pause(THINK_TIME))
                    .exec(AgentJourney.ownerChain(config, fixture, AGENT_PREFIX))
                    .arrive(at)
            }
        val offboardingFixtures = OffboardingFixture.parse(fixtureJson("gatling.offboarding.fixturePath"), offboardingTimes.size)
        val offboarding =
            offboardingFixtures.zip(offboardingTimes) { fixture, at ->
                scenario("Ramped property deregistration ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, OFFBOARDING_LOGIN_PREFIX, ""))
                    .exec(pause(THINK_TIME))
                    .exec(OffboardingJourney.chain(config, fixture, OFFBOARDING_PREFIX))
                    .arrive(at)
            }

        val populations =
            individuals + propertyRegistrations + saved + completed + compliance + organisations + joints + agents +
                councils + operators + offboarding
        val coverageAssertions =
            listOf(
                details("$INDIVIDUAL_PREFIX${IndividualLandlordRegistrationJourney.DASHBOARD_REQUEST}")
                    .successfulRequests().count().`is`(individualTimes.size.toLong()),
                details(SavedRegistrationJourney.requestNames.last()).successfulRequests().count().`is`(savedTimes.size.toLong()),
                details(CompletedPropertyUpdateJourney.requestNames.last()).successfulRequests().count().`is`(completedTimes.size.toLong()),
                details(OrganisationProfileJourney.requestNames.last()).successfulRequests().count().`is`(organisationTimes.size.toLong()),
                details("$JOINT_PREFIX${JointLandlordJourney.requestNames.last()}").successfulRequests().count().`is`(jointTimes.size.toLong()),
                details("$AGENT_PREFIX${AgentJourney.ownerRequestNames.last()}").successfulRequests().count().`is`(agentTimes.size.toLong()),
                details("${COUNCIL_PREFIX}Council access removal: persisted deletion").successfulRequests().count()
                    .`is`(councilTimes.size.toLong()),
                details("$OFFBOARDING_PREFIX${OffboardingJourney.requestNames.last()}")
                    .successfulRequests().count().`is`(offboardingTimes.size.toLong()),
            ) + OperatorJourney.operationRequestNames.map { name ->
                details("$OPERATOR_PREFIX$name").successfulRequests().count().`is`(operatorTimes.size.toLong())
            } + ComplianceUpdateJourney.completedPropertyKinds.map { kind ->
                details(ComplianceUpdateJourney.requestNames(kind).last()).successfulRequests().count().`is`(complianceTimes.size.toLong())
            } + RegistrationJourney.requiredRequests.map { name ->
                details("$PROPERTY_REGISTRATION_PREFIX$name").successfulRequests().count().`is`(registrationTimes.size.toLong())
            }
        setUp(populations)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(listOf(global().failedRequests().percent().`is`(0.0)) + coverageAssertions)
    }

    private fun ScenarioBuilder.arrive(millis: Long): PopulationBuilder = injectOpen(nothingFor(Duration.ofMillis(millis)), atOnceUsers(1))

    private fun decimal(
        properties: Map<String, String>,
        key: String,
        default: Double,
    ): Double = properties[key]?.toDouble() ?: default

    private companion object {
        const val SECONDS_PER_MINUTE = 60
        val THINK_TIME: Duration = Duration.ofSeconds(2)
        const val INDIVIDUAL_PREFIX = "Ramped individual landlord: "
        const val PROPERTY_REGISTRATION_PREFIX = "Ramped property: "
        const val INDIVIDUAL_LOGIN_PREFIX = "Individual landlord authentication"
        const val PROPERTY_REGISTRATION_LOGIN_PREFIX = "Property registration authentication"
        const val SAVED_LOGIN_PREFIX = "Saved registration authentication"
        const val PROPERTY_LOGIN_PREFIX = "Completed property authentication"
        const val COMPLIANCE_LOGIN_PREFIX = "Certificates and compliance authentication"
        const val ORGANISATION_LOGIN_PREFIX = "Organisation authentication"
        const val JOINT_LOGIN_PREFIX = "Joint landlord authentication"
        const val AGENT_LOGIN_PREFIX = "Letting agent owner authentication"
        const val OFFBOARDING_LOGIN_PREFIX = "Offboarding authentication"
        const val JOINT_PREFIX = "Ramped "
        const val AGENT_PREFIX = "Ramped "
        const val OFFBOARDING_PREFIX = "Ramped "
        const val COUNCIL_PREFIX = "Ramped local council: "
        const val OPERATOR_PREFIX = "Ramped system operator: "
        const val OPERATOR_LOGIN_PREFIX = "Operator authentication"
    }
}
