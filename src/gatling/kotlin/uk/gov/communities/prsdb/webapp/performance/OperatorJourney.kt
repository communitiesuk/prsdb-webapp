package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import uk.gov.communities.prsdb.webapp.constants.SAFE_CHARACTERS_CHARSET
import java.time.LocalDate

object OperatorJourney {
    fun passcodes(
        config: BasicRunConfig,
        prefix: String = "",
    ): ChainBuilder {
        val route = "/system-operator/generate-passcode"
        return CouncilHttpForms.form(config, "${prefix}Operator passcode: form", route, route)
            .exec(
                http("${prefix}Operator passcode: current").get(route).disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(css(".govuk-panel__body strong").transform(::validatedPasscode).saveAs("operatorOldPasscode")),
            ).exitHereIfFailed()
            .exec(
                http("${prefix}Operator passcode: generate").post("#{councilFormAction}").disableFollowRedirect()
                    .formParamMap { session ->
                        val names = session.getList<String>("councilHiddenNames")
                        val values = session.getList<String>("councilHiddenValues")
                        require(names.size == values.size)
                        names.zip(values).toMap()
                    }
                    .check(status().`is`(200))
                    .check(
                        css(".govuk-panel__body strong").transform(::validatedPasscode)
                            .not { session -> session.getString("operatorOldPasscode")!! }.saveAs("operatorNewPasscode"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http("${prefix}Operator passcode: persisted").get(route).disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(
                        css(".govuk-panel__body strong").transform(::validatedPasscode).`is` {
                                session ->
                            session.getString("operatorNewPasscode")!!
                        },
                    ),
            ).exitHereIfFailed()
            .exec(
                CouncilHttpForms.form(
                    config,
                    "${prefix}Operator passcode: validation form",
                    "/landlord/passcode-entry",
                    "/landlord/passcode-entry",
                ),
            )
            .exec(
                http("${prefix}Operator passcode: database validation").post("#{councilFormAction}").disableFollowRedirect()
                    .formParamMap { session ->
                        val names = session.getList<String>("councilHiddenNames")
                        val values = session.getList<String>("councilHiddenValues")
                        require(names.size == values.size)
                        names.zip(values).toMap() + mapOf("passcode" to session.getString("operatorNewPasscode"))
                    }.check(status().`is`(302))
                    .check(
                        header("Location").transform {
                            CouncilHttpForms.destination(it, config, "/landlord/register-as-a-landlord")
                        }.exists(),
                    ),
            ).exitHereIfFailed()
    }

    private fun validatedPasscode(value: String): String {
        val passcode = value.trim()
        require(passcode.length == 6 && passcode.all { it in SAFE_CHARACTERS_CHARSET }) { "Invalid generated passcode" }
        return passcode
    }

    fun metrics(
        config: BasicRunConfig,
        prefix: String = "",
    ): ChainBuilder {
        val to = LocalDate.now().minusDays(1)
        val from = to.minusMonths(1)
        return CouncilHttpForms.form(
            config,
            "${prefix}Operator metrics: reporting period form",
            "/system-operator/metrics",
            "/system-operator/metrics",
        ).exec(
            http("${prefix}Operator metrics: reporting period results").post("#{councilFormAction}").disableFollowRedirect()
                .formParamMap { session ->
                    val names = session.getList<String>("councilHiddenNames")
                    val values = session.getList<String>("councilHiddenValues")
                    require(names.size == values.size)
                    names.zip(values).toMap() +
                        mapOf(
                            "fromDay" to from.dayOfMonth.toString(),
                            "fromMonth" to from.monthValue.toString(),
                            "fromYear" to from.year.toString(),
                            "toDay" to to.dayOfMonth.toString(), "toMonth" to to.monthValue.toString(), "toYear" to to.year.toString(),
                        )
                }.check(status().`is`(200))
                .check(css(".govuk-error-summary").notExists())
                .check(css(".govuk-summary-list__row").count().gte(19))
                .check(css("input[name=fromYear]", "value").`is`(from.year.toString()))
                .check(css("input[name=toYear]", "value").`is`(to.year.toString())),
        ).exitHereIfFailed()
    }

    fun dashboard(
        config: BasicRunConfig,
        prefix: String = "",
    ): ChainBuilder =
        exec(
            http("${prefix}Operator: dashboard").get("/system-operator/dashboard").disableFollowRedirect()
                .check(status().`is`(200))
                .check(css("a[href='/system-operator/invite-lc-admin']").exists())
                .check(css("a[href='/system-operator/metrics']").exists()),
        ).exitHereIfFailed()
            .exec(
                http("${prefix}Operator: council administrators").get("/system-operator/manage-local-council-admins")
                    .disableFollowRedirect().check(status().`is`(200))
                    .check(css("a[href='/system-operator/invite-lc-admin'], a[href='invite-lc-admin']").exists()),
            ).exitHereIfFailed()
            .exec(
                CouncilHttpForms.form(
                    config,
                    "${prefix}Operator: invitation form",
                    "/system-operator/invite-lc-admin",
                    "/system-operator/invite-lc-admin",
                ),
            )
            .exec(
                CouncilHttpForms.form(
                    config,
                    "${prefix}Operator metrics: form",
                    "/system-operator/metrics",
                    "/system-operator/metrics",
                ),
            )
            .exec(
                http("${prefix}Operator metrics: invalid reporting period").post("#{councilFormAction}")
                    .disableFollowRedirect()
                    .formParamMap { session ->
                        val names = session.getList<String>("councilHiddenNames")
                        val values = session.getList<String>("councilHiddenValues")
                        require(names.size == values.size)
                        names.zip(values).toMap() +
                            mapOf("fromDay" to "32", "fromMonth" to "13", "fromYear" to "2026")
                    }
                    .check(status().`is`(200))
                    .check(css(".govuk-error-summary").exists())
                    .check(css("main form .govuk-summary-list__row").notExists()),
            ).exitHereIfFailed()
}
