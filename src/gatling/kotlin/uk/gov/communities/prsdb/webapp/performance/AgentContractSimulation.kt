package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentInvitationController
import java.net.URI

class AgentContractSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config =
            BasicRunConfig.from(properties)
        val warmUp = AgentFixture("owner-warmup", 101, "1 Fixture Road", null, null, null)
        val measured = AgentFixture("owner-measured", 102, "2 Fixture Road", null, null, null)
        val contractMode = properties["gatling.agent.contractMode"] ?: "owner"
        val contractScenario =
            when (contractMode) {
                "owner" ->
                    scenario("Letting-agent HTTP contract")
                        .exec(AgentJourney.ownerChain(config, warmUp, "Warm-up: "))
                        .exec(AgentJourney.ownerChain(config, measured))

                "existing-password" ->
                    scenario("Existing-password letting-agent HTTP contract")
                        .exec(AgentJourney.accessAndUpdateChain(config, existingPasswordFixture, requiredPassword(properties)))

                "wrong-password" ->
                    scenario("Wrong-password letting-agent HTTP contract")
                        .exec(wrongPasswordChain(config, requiredPassword(properties)))

                "invalid-token" ->
                    scenario("Invalid letting-agent token HTTP contract")
                        .exec(invalidTokenChain(config))

                else -> error("Unknown letting-agent contract mode: $contractMode")
            }
        val requestNames =
            when (contractMode) {
                "owner" -> AgentJourney.ownerRequestNames
                "existing-password" -> AgentJourney.accessRequestNames(hasPassword = true)
                "wrong-password" -> wrongPasswordRequestNames
                "invalid-token" -> invalidTokenRequestNames
                else -> error("Unknown letting-agent contract mode: $contractMode")
            }
        setUp(
            contractScenario
                .injectOpen(atOnceUsers(1)),
        ).protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"))
            .assertions(BasicPerformanceAssertions.forRequests(config, requestNames))
    }

    private fun wrongPasswordChain(
        config: BasicRunConfig,
        password: String,
    ): ChainBuilder {
        val invitationRoute = LettingAgentInvitationController.LETTING_AGENT_INVITATION_ROUTE
        return exec(
            http(wrongPasswordRequestNames[0])
                .get("$invitationRoute?token=$EXISTING_PASSWORD_TOKEN")
                .disableFollowRedirect()
                .check(status().`is`(302))
                .check(header("Location").transform { HttpDestination.resolve(it, config.baseUrl) }.saveAs("agentNext")),
        ).exitHereIfFailed()
            .exec(
                http(wrongPasswordRequestNames[1])
                    .get("#{agentNext}")
                    .disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(header("Location").transform { HttpDestination.resolve(it, config.baseUrl) }.saveAs("agentNext")),
            ).exitHereIfFailed()
            .exec(
                http(wrongPasswordRequestNames[2])
                    .get("#{agentNext}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(css("form[method=post]", "action").saveAs("agentPasswordFormAction"))
                    .check(css("form input[type=hidden]", "name").findAll().saveAs("agentPasswordHiddenNames"))
                    .check(css("form input[type=hidden]", "value").findAll().saveAs("agentPasswordHiddenValues"))
                    .check(css("form input[name=password]", "name").saveAs("agentPasswordField")),
            ).exitHereIfFailed()
            .exec(
                http(wrongPasswordRequestNames[3])
                    .post { session ->
                        JointAgentJourneySupport.formAction(
                            requireNotNull(session.getString("agentPasswordFormAction")),
                            config,
                            requireNotNull(session.getString("agentNext")),
                        )
                    }.disableFollowRedirect()
                    .formParamMap { session ->
                        val names = session.getList<String>("agentPasswordHiddenNames")
                        val values = session.getList<String>("agentPasswordHiddenValues")
                        require(names.size == values.size) { "A letting-agent password form omitted a hidden field value" }
                        names.zip(values).toMap() + (requireNotNull(session.getString("agentPasswordField")) to password)
                    }.check(status().`is`(200))
                    .check(css(".govuk-error-message").exists()),
            ).exitHereIfFailed()
    }

    private fun invalidTokenChain(config: BasicRunConfig): ChainBuilder {
        val invitationRoute = LettingAgentInvitationController.LETTING_AGENT_INVITATION_ROUTE
        return exec(
            http(invalidTokenRequestNames.single())
                .get("$invitationRoute?token=$INVALID_TOKEN")
                .disableFollowRedirect()
                .check(status().`is`(302))
                .check(
                    header("Location")
                        .transform { URI.create(HttpDestination.resolve(it, config.baseUrl)).path }
                        .`is`(LettingAgentInvitationController.LETTING_AGENT_INVITATION_INVALID_LINK_ROUTE),
                ),
        ).exitHereIfFailed()
    }

    private fun requiredPassword(properties: Map<String, String>): String =
        requireNotNull(properties["gatling.agent.password"]?.takeIf { it.isNotBlank() }) {
            "gatling.agent.password must provide the letting-agent password for this contract"
        }

    companion object {
        const val EXISTING_PASSWORD_TOKEN = "00000000-0000-0000-0000-000000000201"
        const val INVALID_TOKEN = "00000000-0000-0000-0000-000000000299"
        const val EXISTING_PASSWORD_PROPERTY_ID = 201L
        const val EXISTING_PASSWORD_ADDRESS = "201 Existing Password Road"
        const val EXISTING_PASSWORD_EMAIL = "agent-201@example.test"

        private val existingPasswordFixture =
            AgentFixture(
                "owner-existing-password",
                EXISTING_PASSWORD_PROPERTY_ID,
                EXISTING_PASSWORD_ADDRESS,
                EXISTING_PASSWORD_TOKEN,
                EXISTING_PASSWORD_EMAIL,
                true,
            )

        private val wrongPasswordRequestNames =
            listOf(
                "Letting agent: open invitation with incorrect password",
                "Letting agent: resolve existing-password invitation",
                "Letting agent: existing-password form",
                "Letting agent: reject incorrect password",
            )

        private val invalidTokenRequestNames = listOf("Letting agent: reject invalid invitation token")
    }
}
