package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import java.nio.file.Files
import java.nio.file.Path

class OperatorOperationsSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        require(properties["gatling.council.emailDeliveryStub"] == "true") {
            "Operator invitations require gatling.council.emailDeliveryStub=true"
        }
        val fixturePath =
            requireNotNull(properties["gatling.operator.fixturePath"]) {
                "gatling.operator.fixturePath must identify a fresh export from data-nft-operator-fixtures.sql"
            }
        val fixtures = OperatorFixture.parse(Files.readString(Path.of(fixturePath)), config.iterations + 1)
        val includePasscodes = properties["gatling.operator.passcodes"] == "true"
        val includeMetrics = properties["gatling.operator.localMetricsDependencies"] == "true"
        val populations =
            fixtures.mapIndexed { pass, fixture ->
                val prefix = if (pass == 0) "Warm-up: " else ""
                var chain =
                    OneLoginAuthentication.loginChain(config, fixture.subject, "Operator authentication", prefix)
                        .exec(OperatorJourney.dashboard(config, prefix))
                        .exec(
                            CouncilHttpForms.accessChange(
                                config,
                                "/system-operator/edit-admin/${fixture.targetId}",
                                "/system-operator/manage-local-council-admins",
                                fixture.targetName,
                                fixture.targetManager,
                                prefix,
                            ),
                        )
                        .exec(CouncilManagementJourney.invitation(config, fixture.councilId, true, prefix))
                        .exec(
                            CouncilManagementJourney.accessRemoval(
                                config,
                                "/system-operator",
                                fixture.targetId,
                                fixture.targetName,
                                prefix,
                                admin = true,
                            ),
                        )
                if (includePasscodes) chain = chain.exec(OperatorJourney.passcodes(config, prefix))
                if (includeMetrics) chain = chain.exec(OperatorJourney.metrics(config, prefix))
                scenario("Operator operations ${if (pass == 0) "warm-up" else "iteration $pass"}")
                    .exec(chain).injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        val requestNames =
            listOf(
                "Operator: dashboard", "Operator: council administrators", "Operator: invitation form",
                "Operator metrics: form", "Operator metrics: invalid reporting period",
                "Council access 1: persisted role", "Council access 2: persisted role",
                "Council invitation: confirmation", "Council invitation: persisted cancellation",
                "Council access removal: persisted deletion",
            ) + (
                if (includePasscodes) {
                    listOf("Operator passcode: generate", "Operator passcode: persisted", "Operator passcode: database validation")
                } else {
                    emptyList()
                }
            ) +
                (if (includeMetrics) listOf("Operator metrics: reporting period results") else emptyList())
        setUp(populations).protocols(
            http.baseUrl(config.baseUrl).acceptHeader("text/html").userAgentHeader("PRSDB-Gatling-Performance-Tests"),
        ).assertions(BasicPerformanceAssertions.forRequests(config, requestNames))
    }
}
