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
import io.gatling.javaapi.http.HttpDsl.flushCookieJar
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

// Council authentication is deferred; the ten included families use an exclusive fixture per arrival.
class RampedLoadSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties).copy(thinkTime = THINK_TIME)
        val agentPassword = requireNotNull(properties["gatling.agent.password"]?.takeIf { it.isNotBlank() }) {
            "gatling.agent.password must configure a disposable password for prepared agent access"
        }
        val runId = requireNotNull(properties["gatling.organisation.runId"]?.takeIf { it.matches(Regex("[a-z0-9-]{1,40}")) }) {
            "gatling.organisation.runId must identify this run's prepared invitations"
        }
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
        val operatorTimes = schedules.getValue(RampedJourneyFamily.SYSTEM_OPERATOR).arrivalTimesMillis()
        val offboardingTimes = schedules.getValue(RampedJourneyFamily.OFFBOARDING).arrivalTimesMillis()
        require(properties["gatling.council.emailDeliveryStub"] == "true") {
            "Operator invitations require gatling.council.emailDeliveryStub=true"
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
        val reservedSubjects = mutableSetOf<String>()
        val reservedProperties = mutableSetOf<Long>()
        fun reserve(subjects: List<String>, propertyIds: List<Long> = emptyList()) {
            require(subjects.all { reservedSubjects.add(it) }) { "Mixed workload families share a mutable actor" }
            require(propertyIds.all { reservedProperties.add(it) }) { "Mixed workload families share a mutable property" }
        }
        reserve(registrationFixtures.map { it.subject })
        val individuals =
            individualTimes.mapIndexed { index, at ->
                val data = BasicIterationData.forPass(registrationSubject, index + 1)
                val options = IndividualLandlordRegistrationJourney.Options(requestNamePrefix = INDIVIDUAL_PREFIX)
                scenario("Ramped individual landlord registration ${index + 1}")
                    .exec(OneLoginAuthentication.loginChain(config, data.registrationSubject, INDIVIDUAL_LOGIN_PREFIX, ""))
                    .exec(IndividualLandlordRegistrationJourney.chain(config, data, options))
                    .exec(pause(THINK_TIME))
                    .exec(OneLoginAuthentication.landlordDetailsChain(config, INDIVIDUAL_PREFIX))
                    .exec(LandlordPhoneUpdateJourney.chain(config, data.copy(requestNamePrefix = INDIVIDUAL_PREFIX)))
                    .arrive(at)
            }
        val propertyRegistrations =
            registrationFixtures.zip(registrationTimes) { fixture, at ->
                scenario("Ramped property registration ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, PROPERTY_REGISTRATION_LOGIN_PREFIX, ""))
                    .exec(
                        RegistrationJourney.chain(
                            config,
                            fixture,
                            RegistrationVariant.OCCUPIED_SELECTED_CERTIFICATES,
                            paymentBaseUrl,
                            certificatePath = certificatePath,
                            prefix = PROPERTY_REGISTRATION_PREFIX,
                        ),
                    ).arrive(at)
            }
        val saved =
            SavedRegistrationFixture.parse(fixtureJson("gatling.saved.fixturePath"), offset + savedTimes.size).drop(offset)
                .also { reserve(it.map { fixture -> fixture.subject }) }
                .zip(savedTimes) { fixture, at ->
                    scenario("Ramped saved registration ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, SAVED_LOGIN_PREFIX, ""))
                        .exec(SavedRegistrationJourney.chain(config, fixture, ""))
                        .arrive(at)
                }
        val completedPropertyFixtures =
            CompletedPropertyFixture.parse(
                fixtureJson("gatling.property.fixturePath"),
                offset + completedTimes.size + complianceTimes.size,
            ).drop(offset).also {
                reserve(it.map { fixture -> fixture.subject }, it.map { fixture -> fixture.propertyOwnershipId })
            }
        val completed =
            completedPropertyFixtures.take(completedTimes.size).zip(completedTimes) { fixture, at ->
                    scenario("Ramped completed property ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, PROPERTY_LOGIN_PREFIX, ""))
                        .exec(PropertyBranchJourney.chain(config, fixture, ""))
                        .exec(pause(THINK_TIME))
                        .exec(CompletedPropertyUpdateJourney.chain(config, fixture, ""))
                        .arrive(at)
                }
        val compliance =
            completedPropertyFixtures.drop(completedTimes.size).take(complianceTimes.size).zip(complianceTimes) { fixture, at ->
                scenario("Ramped certificates and compliance ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, COMPLIANCE_LOGIN_PREFIX, ""))
                    .exec(ComplianceUpdateJourney.completedPropertyChain(config, fixture, certificatePath))
                    .arrive(at)
            }
        val organisations =
            OrganisationFixture.parse(fixtureJson("gatling.organisation.fixturePath"), organisationTimes.size)
                .also {
                    require(it.all { fixture -> fixture.invitationToken != null }) { "Organisation team invitations must be prepared via HTTP" }
                    reserve(it.map { fixture -> fixture.subject })
                }
                .zip(organisationTimes) { fixture, at ->
                    val subject = "$registrationSubject-organisation-${fixture.organisationId}"
                    scenario("Ramped organisation registration profile and team ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, subject, "Organisation registration authentication", ""))
                        .exec(OrganisationRegistrationJourney.journey(config, subject, "company", ORGANISATION_PREFIX))
                        .exec(flushCookieJar())
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, ORGANISATION_LOGIN_PREFIX, ""))
                        .exec(OrganisationProfileJourney.chain(config, fixture, ORGANISATION_PREFIX))
                        .exec(OrganisationOperationsJourney.team(config, fixture, "$runId-load", ORGANISATION_PREFIX))
                        .exec(OrganisationOperationsJourney.changeAccessLevel(config, ORGANISATION_PREFIX))
                        .exec(flushCookieJar())
                        .exec(OrganisationOperationsJourney.accept(config, fixture.copy(name = OrganisationProfileJourney.NAME), runId, ORGANISATION_PREFIX))
                        .arrive(at)
                }

        val operators =
            OperatorFixture.parse(fixtureJson("gatling.operator.fixturePath"), operatorOffset + operatorTimes.size).drop(operatorOffset)
                .also { reserve(it.flatMap { fixture -> listOf(fixture.subject, fixture.targetSubject) }) }
                .zip(operatorTimes) { fixture, at ->
                    scenario("Ramped operator ${fixture.subject}")
                        .exec(OneLoginAuthentication.loginChain(config, fixture.subject, OPERATOR_LOGIN_PREFIX, ""))
                        .exec(OperatorJourney.operations(config, fixture, OPERATOR_PREFIX))
                        .arrive(at)
                }

        val joints =
            JointLandlordFixture.parseInvitations(fixtureJson("gatling.joint.fixturePath"), jointTimes.size)
                .also {
                    reserve(
                        it.flatMap { fixture -> listOf(fixture.ownerSubject, requireNotNull(fixture.inviteeSubject)) },
                        it.map { fixture -> fixture.propertyOwnershipId },
                    )
                }.zip(jointTimes) { fixture, at ->
                scenario("Ramped joint landlord acceptance ${fixture.inviteeSubject}")
                    .exec(OneLoginAuthentication.loginChain(config, requireNotNull(fixture.inviteeSubject), JOINT_LOGIN_PREFIX, ""))
                    .exec(JointLandlordJourney.decisionChain(config, fixture, JOINT_PREFIX))
                    .arrive(at)
            }
        val agents =
            AgentFixture.parseAccessFixtures(fixtureJson("gatling.agent.fixturePath"), agentTimes.size, hasPassword = false)
                .also {
                    reserve(it.map { fixture -> fixture.ownerSubject }, it.map { fixture -> fixture.propertyOwnershipId })
                }.zip(agentTimes) { fixture, at ->
                scenario("Ramped letting agent access and update ${fixture.ownerSubject}")
                    .exec(AgentJourney.accessAndUpdateChain(config, fixture, agentPassword, AGENT_PREFIX))
                    .arrive(at)
            }
        val offboardingFixtures = OffboardingFixture.parse(fixtureJson("gatling.offboarding.fixturePath"), offboardingTimes.size)
        reserve(offboardingFixtures.map { it.subject }, offboardingFixtures.map { it.propertyOwnershipId })
        val offboarding =
            offboardingFixtures.zip(offboardingTimes) { fixture, at ->
                scenario("Ramped property deregistration ${fixture.subject}")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, OFFBOARDING_LOGIN_PREFIX, ""))
                    .exec(OffboardingJourney.chain(config, fixture, OFFBOARDING_PREFIX))
                    .arrive(at)
            }

        val populations =
            individuals + propertyRegistrations + saved + completed + compliance + organisations + joints + agents +
                operators + offboarding
        val coverageAssertions =
            listOf(
                details("$INDIVIDUAL_PREFIX${IndividualLandlordRegistrationJourney.DASHBOARD_REQUEST}")
                    .successfulRequests().count().`is`(individualTimes.size.toLong()),
                details("$INDIVIDUAL_PREFIX${LandlordPhoneUpdateJourney.DETAILS_REQUEST}")
                    .successfulRequests().count().`is`(individualTimes.size.toLong()),
                details(SavedRegistrationJourney.requestNames.last()).successfulRequests().count().`is`(savedTimes.size.toLong()),
                details(CompletedPropertyUpdateJourney.requestNames.last()).successfulRequests().count().`is`(completedTimes.size.toLong()),
                details("$ORGANISATION_PREFIX${OrganisationProfileJourney.requestNames.last()}")
                    .successfulRequests().count().`is`(organisationTimes.size.toLong()),
                details("$ORGANISATION_PREFIX${OrganisationRegistrationJourney.requestNames("company").last()}")
                    .successfulRequests().count().`is`(organisationTimes.size.toLong()),
                details("$ORGANISATION_PREFIX${OrganisationOperationsJourney.acceptRequestNames.last()}")
                    .successfulRequests().count().`is`(organisationTimes.size.toLong()),
                details("$ORGANISATION_PREFIX${OrganisationOperationsJourney.accessLevelRequestNames.last()}")
                    .successfulRequests().count().`is`(organisationTimes.size.toLong()),
                details("$ORGANISATION_PREFIX${OrganisationOperationsJourney.teamRequestNames.last()}")
                    .successfulRequests().count().`is`(organisationTimes.size.toLong()),
                details("$JOINT_PREFIX${JointLandlordJourney.decisionRequestNames.last()}")
                    .successfulRequests().count().`is`(jointTimes.size.toLong()),
                details("$AGENT_PREFIX${AgentJourney.accessRequestNames(false).last()}").successfulRequests().count().`is`(agentTimes.size.toLong()),
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
        const val ORGANISATION_PREFIX = "Ramped organisation: "
        const val OFFBOARDING_LOGIN_PREFIX = "Offboarding authentication"
        const val JOINT_PREFIX = "Ramped "
        const val AGENT_PREFIX = "Ramped "
        const val OFFBOARDING_PREFIX = "Ramped "
        const val OPERATOR_PREFIX = "Ramped system operator: "
        const val OPERATOR_LOGIN_PREFIX = "Operator authentication"
    }
}
