package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

object OneLoginAuthentication {
    const val START_REQUEST = "One Login: start"
    const val CALLBACK_REQUEST = "One Login: callback"
    const val DETAILS_REQUEST = "Landlord: details"
    val applicationRequestNames = listOf(START_REQUEST, CALLBACK_REQUEST, DETAILS_REQUEST)

    fun chain(config: BasicRunConfig): ChainBuilder =
        exec(
            http(START_REQUEST)
                .get("/oauth2/authorization/one-login")
                .disableFollowRedirect()
                .check(status().`is`(302))
                .check(header("Location").transform { HttpDestination.resolve(it, config.simulatorUrl) }.saveAs("authorizeUrl")),
        ).exitHereIfFailed()
            .exec(
                http("One Login simulator: authorize")
                    .get("#{authorizeUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                    .check(
                        css("form[method=post]", "action")
                            .transform { HttpDestination.resolve(it, config.simulatorUrl) }
                            .saveAs("loginFormAction"),
                    )
                    .check(css("form input[type=hidden]", "name").findAll().saveAs("loginHiddenNames"))
                    .check(css("form input[type=hidden]", "value").findAll().saveAs("loginHiddenValues"))
                    .check(css("form textarea", "name").findAll().saveAs("loginTextareaNames"))
                    .check(css("form textarea").findAll().saveAs("loginTextareaValues"))
                    .check(css("input[name=authCode]", "value").exists())
                    .check(css("input[name=authRequestParams]", "value").exists())
                    .check(css("input[name=state]", "value").exists())
                    .check(css("input[name=email]", "value").saveAs("loginEmail"))
                    .check(css("input[name=phoneNumber]", "value").saveAs("loginPhone"))
                    .check(css("input[name=maxLoCAchieved]", "value").saveAs("loginConfidence")),
            ).exitHereIfFailed()
            .exec(
                http("One Login simulator: submit")
                    .post("#{loginFormAction}")
                    .disableFollowRedirect()
                    .formParamMap { session ->
                        val names = session.getList<String>("loginHiddenNames")
                        val values = session.getList<String>("loginHiddenValues")
                        val textareaNames = session.getList<String>("loginTextareaNames")
                        val textareaValues = session.getList<String>("loginTextareaValues")
                        require(names.size == values.size) { "Simulator hidden form fields have missing values" }
                        require(textareaNames.size == textareaValues.size) { "Simulator textarea fields have missing values" }
                        names.zip(values).toMap() + textareaNames.zip(textareaValues).toMap() +
                            mapOf(
                                "sub" to config.landlordSubject,
                                "email" to session.getString("loginEmail"),
                                "emailVerified" to "true",
                                "phoneNumber" to session.getString("loginPhone"),
                                "phoneNumberVerified" to "true",
                                "maxLoCAchieved" to session.getString("loginConfidence"),
                                "continue" to "continue",
                            )
                    }.check(status().`is`(302))
                    .check(header("Location").transform { HttpDestination.resolve(it, config.baseUrl) }.saveAs("callbackUrl")),
            ).exitHereIfFailed()
            .exec(
                http(CALLBACK_REQUEST)
                    .get("#{callbackUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(302)),
            ).exitHereIfFailed()
            .exec(
                http(DETAILS_REQUEST)
                    .get("/landlord/landlord-details")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                    .check(
                        css("a[href*='/landlord-details/update-phone-number']", "href")
                            .transform { HttpDestination.resolve(it, config.baseUrl) }
                            .saveAs("phoneUpdateEntry"),
                    ),
            ).exitHereIfFailed()
}
