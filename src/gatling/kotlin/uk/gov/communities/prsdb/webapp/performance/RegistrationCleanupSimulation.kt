package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.CoreDsl.atOnceUsers
import io.gatling.javaapi.core.CoreDsl.bodyString
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.Simulation
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path

class RegistrationCleanupSimulation : Simulation() {
    init {
        val properties = System.getProperties().stringPropertyNames().associateWith { System.getProperty(it) }
        val config = BasicRunConfig.from(properties)
        require(config.target == PerformanceTarget.LOCAL)
        val offset = requireNotNull(properties["gatling.registration.fixtureOffset"]).toInt()
        require(offset >= 0)
        val contexts = requireNotNull(properties["gatling.registration.cleanupContexts"]).split(',')
        require(contexts.size == config.iterations + 1 && contexts.distinct().size == contexts.size)
        require(contexts.all { it.matches(Regex("[A-Za-z0-9_-]+")) })
        val fixtures =
            RegistrationFixture.parse(
                Files.readString(Path.of(requireNotNull(properties["gatling.registration.fixturePath"]))),
                offset + contexts.size,
            ).drop(offset)
        val populations =
            fixtures.mapIndexed { index, fixture ->
                val context = contexts[index]
                val delete = "${config.baseUrl}/landlord/delete-incomplete-property?contextId=$context"
                val confirmation = "${config.baseUrl}/landlord/delete-incomplete-property/confirmation?contextId=$context"
                val link = "a[href*='/delete-incomplete-property?'][href*='contextId=$context']"
                scenario("Cleanup allocated incomplete registration $index")
                    .exec(OneLoginAuthentication.loginChain(config, fixture.subject, "Cleanup authentication"))
                    .exec(
                        http("Cleanup: allocated incomplete record").get("/landlord/incomplete-properties")
                            .disableFollowRedirect().check(status().`is`(200)).check(css(link, "href").saveAs("cleanupLink")),
                    )
                    .exitHereIfFailed()
                    .exec { session ->
                        val url = HttpDestination.resolve(requireNotNull(session.getString("cleanupLink")), config.baseUrl)
                        require(URI.create(url).path == URI.create(delete).path)
                        require(URI.create(url).rawQuery.orEmpty().split('&').count { it == "contextId=$context" } == 1)
                        session
                    }.exitHereIfFailed()
                    .exec(
                        http("Cleanup: confirmation form").get(delete).disableFollowRedirect()
                            .check(status().`is`(200)).check(bodyString().transform { it.contains(fixture.postcode) }.`is`(true))
                            .check(css("form[method=post]", "action").saveAs("cleanupAction"))
                            .check(css("form input[type=hidden]", "name").findAll().saveAs("cleanupNames"))
                            .check(css("form input[type=hidden]", "value").findAll().saveAs("cleanupValues")),
                    )
                    .exitHereIfFailed()
                    .exec { session ->
                        val action = HttpDestination.resolve(requireNotNull(session.getString("cleanupAction")), config.baseUrl, delete)
                        require(action == delete)
                        val names = session.getList<String>("cleanupNames")
                        val values = session.getList<String>("cleanupValues")
                        val fields = RegistrationDestination.hiddenFields(names, values)
                        session.set("cleanupFields", fields + mapOf("wantsToProceed" to "true"))
                    }.exitHereIfFailed()
                    .exec(
                        http("Cleanup: ordinary confirmed deletion").post(delete).disableFollowRedirect()
                            .formParamMap { session -> session.get<Map<String, String>>("cleanupFields")!! }
                            .check(
                                status().`is`(302),
                            ).check(header("Location").transform { HttpDestination.resolve(it, config.baseUrl) }.`is`(confirmation)),
                    )
                    .exitHereIfFailed()
                    .exec(
                        http("Cleanup: durable deletion confirmation").get(confirmation).disableFollowRedirect().check(status().`is`(200)),
                    )
                    .exitHereIfFailed()
                    .exec(
                        http("Cleanup: incomplete record absent").get("/landlord/incomplete-properties").disableFollowRedirect()
                            .check(status().`is`(200)).check(css(link).notExists()),
                    )
                    .injectOpen(atOnceUsers(1))
            }.reduce { previous, next -> previous.andThen(next) }
        setUp(populations).protocols(http.baseUrl(config.baseUrl))
            .assertions(io.gatling.javaapi.core.CoreDsl.global().failedRequests().count().`is`(0L))
    }
}
