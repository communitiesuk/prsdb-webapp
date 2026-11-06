package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.global
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID

class OperatorRegistrationInvitationSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        require(properties["gatling.council.emailDeliveryStub"] == "true") { "Invitation preparation requires stubbed email delivery" }
        val path = requireNotNull(properties["gatling.operator.fixturePath"])
        val fixture = OperatorFixture.parse(Files.readString(Path.of(path)), 1).single()
        val invite = "/system-operator/invite-lc-admin"
        var chain = OneLoginAuthentication.loginChain(config, fixture.subject, "Operator invitation preparation")
        for (index in 1..config.iterations + 1) {
            val email = "000-gatling-registration-${UUID.randomUUID()}@example.com"
            chain =
                chain.exec(CouncilHttpForms.form(config, "Registration invitation $index: form", invite, invite))
                    .exec(
                        CouncilHttpForms.submit(
                            config, "Registration invitation $index: send",
                            mapOf("localCouncilId" to fixture.councilId.toString(), "email" to email, "confirmEmail" to email),
                            "$invite/confirmation",
                        ),
                    )
                    .exec(
                        http("Registration invitation $index: confirmation").get("#{councilNextUrl}").disableFollowRedirect()
                            .check(status().`is`(200)).check(css("main").transform { it.contains(email) }.`is`(true)),
                    ).exitHereIfFailed()
        }
        setUp(scenario("Prepare real council registration invitations").exec(chain).injectOpen(atOnceUsers(1)))
            .protocols(http.baseUrl(config.baseUrl).acceptHeader("text/html"))
            .assertions(global().failedRequests().count().`is`(0L))
    }
}
