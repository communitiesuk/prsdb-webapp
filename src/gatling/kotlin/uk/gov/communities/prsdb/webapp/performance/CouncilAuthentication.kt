package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

object CouncilAuthentication {
    const val PREFIX = "Council internal-access authentication"

    fun chain(
        config: BasicRunConfig,
        subject: String,
        prefix: String = "",
    ): ChainBuilder {
        val key = OneLoginAuthentication.authorizationUrlKey(PREFIX)
        return exec(
            http("$prefix$PREFIX: start").get("/oauth2/authorization/internal-access").disableFollowRedirect()
                .check(status().`is`(302))
                .check(
                    header("Location").transform { HttpDestination.resolve(it, config.simulatorUrl) }.saveAs(key),
                ),
        ).exitHereIfFailed()
            .exec(
                http("Internal-access simulator: authorize").get("#{$key}").disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(
                        css("form[method=post]", "action")
                            .transform { HttpDestination.resolve(it, config.simulatorUrl) }.saveAs("councilAuthenticationAction"),
                    )
                    .check(css("form input[type=hidden]", "name").findAll().saveAs("councilAuthenticationNames"))
                    .check(css("form input[type=hidden]", "value").findAll().saveAs("councilAuthenticationValues"))
                    .check(css("form textarea", "name").findAll().saveAs("councilAuthenticationTextareaNames"))
                    .check(css("form textarea").findAll().saveAs("councilAuthenticationTextareaValues"))
                    .check(css("input[name=authCode]", "value").exists())
                    .check(css("input[name=state]", "value").exists())
                    .check(css("input[name=authRequestParams]", "value").exists())
                    .check(css("input[name=email]", "value").saveAs("councilAuthenticationEmail"))
                    .check(css("input[name=phoneNumber]", "value").saveAs("councilAuthenticationPhone"))
                    .check(css("input[name=maxLoCAchieved]", "value").saveAs("councilAuthenticationConfidence")),
            ).exitHereIfFailed()
            .exec(
                http("Internal-access simulator: submit").post("#{councilAuthenticationAction}").disableFollowRedirect()
                    .formParamMap { session ->
                        val names = session.getList<String>("councilAuthenticationNames")
                        val values = session.getList<String>("councilAuthenticationValues")
                        val textareaNames = session.getList<String>("councilAuthenticationTextareaNames")
                        val textareaValues = session.getList<String>("councilAuthenticationTextareaValues")
                        require(names.size == values.size && textareaNames.size == textareaValues.size) {
                            "Internal-access simulator form fields are missing values"
                        }
                        names.zip(values).toMap() + textareaNames.zip(textareaValues).toMap() +
                            mapOf(
                                "sub" to subject,
                                "email" to session.getString("councilAuthenticationEmail"),
                                "phoneNumber" to session.getString("councilAuthenticationPhone"),
                                "maxLoCAchieved" to session.getString("councilAuthenticationConfidence"),
                                "emailVerified" to "true", "phoneNumberVerified" to "true", "continue" to "continue",
                            )
                    }.check(status().`is`(302))
                    .check(
                        header("Location").transform {
                            CouncilHttpForms.destination(it, config, "/local-council/login/oauth2/code/internal-access")
                        }.saveAs("councilAuthenticationCallback"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http("$prefix$PREFIX: callback").get("#{councilAuthenticationCallback}").disableFollowRedirect()
                    .check(status().`is`(302)),
            ).exitHereIfFailed()
    }
}
