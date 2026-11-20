package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

object CouncilAuthentication {
    const val PREFIX = "Council internal-access authentication"
    private const val AUTHORIZATION_URL_KEY = "councilInternalAccessAuthorizationUrl"

    fun chain(
        config: BasicRunConfig,
        subject: String,
        simulatorUrl: String,
        prefix: String = "",
    ): ChainBuilder {
        require(subject.isNotBlank()) { "A council subject is required" }
        require(simulatorUrl.isNotBlank()) { "gatling.council.simulatorUrl must identify the internal-access simulator" }
        return exec(
            http("$prefix$PREFIX: start")
                .get { _ -> HttpDestination.withCacheBuster("${config.councilBaseUrl}/oauth2/authorization/internal-access") }
                .disableFollowRedirect()
                .check(status().`is`(302))
                .check(
                    header("Location").transform { HttpDestination.resolve(it, simulatorUrl) }.saveAs(AUTHORIZATION_URL_KEY),
                ),
        ).exitHereIfFailed()
            .exec(
                http("Internal-access simulator: authorize").get("#{$AUTHORIZATION_URL_KEY}").disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(
                        css("form[method=post]", "action")
                            .transform { HttpDestination.resolve(it, simulatorUrl) }.saveAs("councilAuthenticationAction"),
                    )
                    .check(css("form input[type=hidden]", "name").findAll().saveAs("councilAuthenticationNames"))
                    .check(css("form input[type=hidden]", "value").findAll().saveAs("councilAuthenticationValues"))
                    .check(css("form textarea", "name").findAll().optional().saveAs("councilAuthenticationTextareaNames"))
                    .check(css("form textarea").findAll().optional().saveAs("councilAuthenticationTextareaValues"))
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
                        val textareaNames = session.getList<String>("councilAuthenticationTextareaNames").orEmpty()
                        val textareaValues = session.getList<String>("councilAuthenticationTextareaValues").orEmpty()
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
                            CouncilHttpForms.destination(
                                it,
                                config,
                                "/local-council/login/oauth2/code/internal-access",
                                baseUrl = config.councilBaseUrl,
                            )
                        }.saveAs("councilAuthenticationCallback"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http("$prefix$PREFIX: callback").get("#{councilAuthenticationCallback}").disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(
                        header("Location").transform {
                            CouncilHttpForms.destination(it, config, "/local-council/dashboard", baseUrl = config.councilBaseUrl)
                        }.saveAs("councilAuthenticationDashboard"),
                    ),
            ).exitHereIfFailed()
    }
}
