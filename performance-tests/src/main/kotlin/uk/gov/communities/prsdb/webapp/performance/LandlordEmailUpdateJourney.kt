package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

object LandlordEmailUpdateJourney {
    const val EMAIL_ADDRESS = "performance-landlord@example.invalid"
    const val START_REQUEST = "Email update: start"
    const val FORM_REQUEST = "Email update: form"
    const val SUBMIT_REQUEST = "Email update: submit"
    const val DETAILS_REQUEST = "Email update: saved details"
    val requestNames = listOf(START_REQUEST, FORM_REQUEST, SUBMIT_REQUEST, DETAILS_REQUEST)

    fun chain(config: BasicRunConfig): ChainBuilder =
        exec(
            http(START_REQUEST)
                .get("#{emailUpdateEntry}")
                .disableFollowRedirect()
                .check(status().`is`(302))
                .check(
                    header("Location")
                        .transform { HttpDestination.resolve(it, config.baseUrl) }
                        .saveAs("emailUpdateFormUrl"),
                ),
        ).exitHereIfFailed()
            .exec(
                http(FORM_REQUEST)
                    .get("#{emailUpdateFormUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                    .check(css(".govuk-error-summary").notExists())
                    .check(css("form[method=post]", "action").saveAs("emailUpdateFormAction"))
                    .check(css("form input[type=hidden]", "name").findAll().saveAs("emailUpdateHiddenNames"))
                    .check(css("form input[type=hidden]", "value").findAll().saveAs("emailUpdateHiddenValues"))
                    .check(css("input[name=emailAddress]").exists()),
            ).exitHereIfFailed()
            .exec(
                http(SUBMIT_REQUEST)
                    .post { session ->
                        HttpDestination.resolve(
                            requireNotNull(session.getString("emailUpdateFormAction")) {
                                "Email update form action is missing"
                            },
                            config.baseUrl,
                            requireNotNull(session.getString("emailUpdateFormUrl")) {
                                "Email update form URL is missing"
                            },
                        )
                    }.disableFollowRedirect()
                    .formParamMap { session ->
                        val names = session.getList<String>("emailUpdateHiddenNames")
                        val values = session.getList<String>("emailUpdateHiddenValues")
                        require(names.size == values.size) { "Email update form has hidden fields with missing values" }
                        names.zip(values).toMap() + mapOf("emailAddress" to EMAIL_ADDRESS)
                    }.check(status().`is`(302))
                    .check(
                        header("Location")
                            .transform { HttpDestination.resolve(it, config.baseUrl) }
                            .saveAs("emailUpdateDetailsUrl"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http(DETAILS_REQUEST)
                    .get("#{emailUpdateDetailsUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                    .check(css(".govuk-error-summary").notExists())
                    .check(
                        css(".govuk-summary-list__row:has(a[href*='/update-email']) .govuk-summary-list__value")
                            .transform { it.trim() }
                            .`is`(EMAIL_ADDRESS),
                    ),
            ).exitHereIfFailed()
}
