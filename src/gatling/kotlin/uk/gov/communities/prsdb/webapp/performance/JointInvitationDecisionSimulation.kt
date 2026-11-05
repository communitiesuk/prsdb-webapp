package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class JointInvitationDecisionSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        val decision =
            requireNotNull(properties["gatling.joint.invitationDecision"]?.takeIf { it.isNotBlank() }) {
                "gatling.joint.invitationDecision must be accept, accept-new-user or reject"
            }
        require(decision in setOf("accept", "accept-new-user", "reject")) {
            "gatling.joint.invitationDecision must be accept, accept-new-user or reject"
        }
        val fixturePath =
            requireNotNull(properties["gatling.joint.fixturePath"]?.takeIf { it.isNotBlank() }) {
                "gatling.joint.fixturePath must identify a read-only existing-invitation export"
            }
        val fixtures = JointLandlordFixture.parseInvitations(Files.readString(Path.of(fixturePath)), config.iterations + 1)
        val accept = decision != "reject"
        val newUser = decision == "accept-new-user"
        val scenarios =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                scenario("Joint invitation $decision ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(
                        OneLoginAuthentication.loginChain(
                            config,
                            requireNotNull(fixture.inviteeSubject),
                            LOGIN_PREFIX,
                            prefix,
                        ),
                    )
                    .exec(
                        JointLandlordJourney.decisionChain(
                            config,
                            fixture,
                            accept,
                            prefix,
                            if (newUser) newUserRegistration(config, fixture, prefix, pass) else null,
                        ),
                    )
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(scenarios)
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(
                BasicPerformanceAssertions.forRequests(
                    config,
                    OneLoginAuthentication.loginRequestNames(LOGIN_PREFIX) +
                        JointLandlordJourney.decisionRequestNames(accept, newUser) +
                        if (newUser) IndividualLandlordRegistrationJourney.requestNames(NEW_USER_REGISTRATION) else emptyList(),
                ),
            )
    }

    private companion object {
        const val LOGIN_PREFIX = "Joint invitee authentication"
        val NEW_USER_REGISTRATION =
            IndividualLandlordRegistrationJourney.Options(
                requestNamePrefix = "Joint invitation: ",
                embeddedEntryUrlKey = JointLandlordJourney.REGISTRATION_ENTRY_KEY,
            )

        fun newUserRegistration(
            config: BasicRunConfig,
            fixture: JointLandlordFixture,
            prefix: String,
            pass: Int,
        ): ChainBuilder {
            val phoneNumber = "0207946${(800 + pass).toString().padStart(4, '0')}"
            return IndividualLandlordRegistrationJourney.chain(
                config,
                BasicIterationData(
                    requestNamePrefix = prefix,
                    profilePhoneNumber = phoneNumber,
                    profileEmailAddress = requireNotNull(fixture.invitedEmail),
                    registrationSubject = requireNotNull(fixture.inviteeSubject),
                    registrationEmailAddress = requireNotNull(fixture.invitedEmail),
                    registrationPhoneNumber = phoneNumber,
                ),
                NEW_USER_REGISTRATION,
            )
        }
    }
}
