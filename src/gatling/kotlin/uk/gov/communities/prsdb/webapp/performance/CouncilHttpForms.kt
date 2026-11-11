package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.UUID

object CouncilHttpForms {
    fun destination(
        location: String,
        config: BasicRunConfig,
        expectedPath: String,
        relativeTo: String = config.baseUrl + expectedPath,
    ): String {
        val resolved = HttpDestination.resolve(location, config.baseUrl, relativeTo)
        require(URI.create(resolved).path == expectedPath) { "Unexpected council/operator destination path" }
        return resolved
    }

    fun form(
        config: BasicRunConfig,
        label: String,
        url: String,
        expectedPath: String,
        identity: String? = null,
        correlateJourney: Boolean = false,
    ): ChainBuilder {
        var request =
            http(label).get(url).queryParam("perfCacheBuster") { UUID.randomUUID().toString() }
                .disableFollowRedirect().check(status().`is`(200))
                .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                .check(
                    css("form[method=post], form[method=POST]", "action")
                        .find().transformWithSession { action, session ->
                            val current = if (url.startsWith("#{")) session.getString(url.substring(2, url.length - 1))!! else url
                            val resolved = destination(action, config, expectedPath, HttpDestination.resolve(current, config.baseUrl))
                            if (correlateJourney) {
                                require(journeyId(resolved) == session.getString("councilJourneyId")) {
                                    "Council registration form changed its journey ID"
                                }
                            }
                            resolved
                        }.saveAs("councilFormAction"),
                )
                .check(css("form input[type=hidden]", "name").findAll().saveAs("councilHiddenNames"))
                .check(css("form input[type=hidden]", "value").findAll().saveAs("councilHiddenValues"))
                .check(css("form input[name=_csrf]", "value").exists())
        if (identity != null) {
            request = request.check(css("main, body").transform { it.contains(identity) }.`is`(true))
        }
        return exec(request).exitHereIfFailed().pause(config.thinkTime)
    }

    fun submit(
        config: BasicRunConfig,
        label: String,
        fields: Map<String, String>,
        expectedRedirectPath: String,
        correlateJourney: Boolean = false,
    ): ChainBuilder =
        exec(
            http(label).post("#{councilFormAction}").disableFollowRedirect()
                .formParamMap { session ->
                    val names = session.getList<String>("councilHiddenNames")
                    val values = session.getList<String>("councilHiddenValues")
                    require(names.size == values.size) { "Missing hidden field values" }
                    names.zip(values).toMap() + fields
                }
                .check(status().`is`(302))
                .check(
                    header("Location").find().transformWithSession { location, session ->
                        val resolved = destination(location, config, expectedRedirectPath, session.getString("councilFormAction")!!)
                        if (correlateJourney && !expectedRedirectPath.endsWith("/confirmation")) {
                            require(journeyId(resolved) == session.getString("councilJourneyId")) {
                                "Council registration redirect changed its journey ID"
                            }
                        }
                        resolved
                    }
                        .saveAs("councilNextUrl"),
                ),
        ).exitHereIfFailed()

    fun accessChange(
        config: BasicRunConfig,
        edit: String,
        manage: String,
        name: String,
        initiallyManager: Boolean,
        prefix: String = "",
    ): ChainBuilder {
        var chain = exec { it }
        for ((index, manager) in listOf(!initiallyManager, initiallyManager).withIndex()) {
            val label = "${prefix}Council access ${index + 1}"
            chain =
                chain.exec(form(config, "$label: form", edit, edit, name))
                    .exec(submit(config, "$label: save", mapOf("isManager" to manager.toString()), manage))
                    .exec(
                        http("$label: persisted role").get(edit).queryParam("perfCacheBuster") { UUID.randomUUID().toString() }
                            .disableFollowRedirect()
                            .check(status().`is`(200))
                            .check(css("input[name=isManager][value=$manager][checked]").exists())
                            .check(css("main, body").transform { it.contains(name) }.`is`(true)),
                    ).exitHereIfFailed()
        }
        return chain
    }

    fun journeyId(url: String): String {
        val identifiers =
            URI.create(url).rawQuery.orEmpty().split("&").map { part ->
                val pair = part.split("=", limit = 2)
                URLDecoder.decode(pair[0], StandardCharsets.UTF_8) to
                    URLDecoder.decode(pair.getOrElse(1) { "" }, StandardCharsets.UTF_8)
            }.filter { it.first == "journeyId" }.map { it.second }
        require(identifiers.size == 1 && identifiers.single().isNotBlank()) { "Expected exactly one nonblank council journey ID" }
        return identifiers.single()
    }
}
