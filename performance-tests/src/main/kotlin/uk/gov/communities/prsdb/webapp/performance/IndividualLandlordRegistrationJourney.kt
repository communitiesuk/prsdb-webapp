package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.header
import io.gatling.javaapi.http.HttpDsl.headerRegex
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status

object IndividualLandlordRegistrationJourney {
    const val START_REQUEST = "Registration: start page"
    const val PRIVACY_INITIALIZE_REQUEST = "Registration: initialize privacy journey"
    const val PRIVACY_FORM_REQUEST = "Registration: privacy preflight form"
    const val PRIVACY_SUBMIT_REQUEST = "Registration: accept privacy notice"
    const val CONFIRM_IDENTITY_FORM_REQUEST = "Registration: confirm identity form"
    const val CONFIRM_IDENTITY_SUBMIT_REQUEST = "Registration: confirm identity"
    const val EMAIL_FORM_REQUEST = "Registration: email form"
    const val EMAIL_SUBMIT_REQUEST = "Registration: submit email"
    const val PHONE_FORM_REQUEST = "Registration: phone form"
    const val PHONE_SUBMIT_REQUEST = "Registration: submit phone"
    const val LANDLORD_TYPE_FORM_REQUEST = "Registration: landlord type form"
    const val LANDLORD_TYPE_SUBMIT_REQUEST = "Registration: submit landlord type"
    const val COUNTRY_FORM_REQUEST = "Registration: country form"
    const val COUNTRY_SUBMIT_REQUEST = "Registration: submit country"
    const val ADDRESS_LOOKUP_FORM_REQUEST = "Registration: address lookup form"
    const val ADDRESS_LOOKUP_SUBMIT_REQUEST = "Registration: submit address lookup"
    const val ADDRESS_SELECTION_FORM_REQUEST = "Registration: address selection form"
    const val ADDRESS_SELECTION_SUBMIT_REQUEST = "Registration: select manual address"
    const val MANUAL_ADDRESS_FORM_REQUEST = "Registration: manual address form"
    const val MANUAL_ADDRESS_SUBMIT_REQUEST = "Registration: submit manual address"
    const val CHECK_ANSWERS_FORM_REQUEST = "Registration: check answers form"
    const val CHECK_ANSWERS_SUBMIT_REQUEST = "Registration: confirm registration"
    const val COMPLETION_REQUEST = "Registration: complete journey"
    const val REAUTHENTICATION_REQUEST = "Registration: refresh authentication"
    const val CONFIRMATION_REQUEST = "Registration: confirmation"
    const val DASHBOARD_REQUEST = "Registration: dashboard"
    const val POST_REGISTRATION_AUTHENTICATION_PREFIX = "Registration confirmation authentication"

    val requestNames =
        listOf(
            START_REQUEST,
            PRIVACY_INITIALIZE_REQUEST,
            PRIVACY_FORM_REQUEST,
            PRIVACY_SUBMIT_REQUEST,
            CONFIRM_IDENTITY_FORM_REQUEST,
            CONFIRM_IDENTITY_SUBMIT_REQUEST,
            EMAIL_FORM_REQUEST,
            EMAIL_SUBMIT_REQUEST,
            PHONE_FORM_REQUEST,
            PHONE_SUBMIT_REQUEST,
            LANDLORD_TYPE_FORM_REQUEST,
            LANDLORD_TYPE_SUBMIT_REQUEST,
            COUNTRY_FORM_REQUEST,
            COUNTRY_SUBMIT_REQUEST,
            ADDRESS_LOOKUP_FORM_REQUEST,
            ADDRESS_LOOKUP_SUBMIT_REQUEST,
            ADDRESS_SELECTION_FORM_REQUEST,
            ADDRESS_SELECTION_SUBMIT_REQUEST,
            MANUAL_ADDRESS_FORM_REQUEST,
            MANUAL_ADDRESS_SUBMIT_REQUEST,
            CHECK_ANSWERS_FORM_REQUEST,
            CHECK_ANSWERS_SUBMIT_REQUEST,
            COMPLETION_REQUEST,
            REAUTHENTICATION_REQUEST,
            CONFIRMATION_REQUEST,
            DASHBOARD_REQUEST,
        ) +
            OneLoginAuthentication.identityVerificationRequestNames +
            listOf(OneLoginAuthentication.loginRequestNames(POST_REGISTRATION_AUTHENTICATION_PREFIX).last())

    fun chain(
        config: BasicRunConfig,
        registrationSubject: String,
    ): ChainBuilder =
        exec(
            http(START_REQUEST)
                .get("/landlord/register-as-a-landlord/start")
                .disableFollowRedirect()
                .check(status().`is`(200))
                .check(headerRegex("Content-Type", "^text/html(?:;.*)?$"))
                .check(
                    css("a.govuk-button", "href")
                        .transform { HttpDestination.resolve(it, config.baseUrl) }
                        .saveAs("registrationPrivacyUrl"),
                ),
        ).exitHereIfFailed()
            .exec(
                http(PRIVACY_INITIALIZE_REQUEST)
                    .get("#{registrationPrivacyUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(
                        header("Location")
                            .transform { HttpDestination.resolve(it, config.baseUrl) }
                            .saveAs("registrationPrivacyPageUrl"),
                    ),
            ).exitHereIfFailed()
            .exec(formPage(config, PRIVACY_FORM_REQUEST, "registrationPrivacyPageUrl", "agreesToPrivacyNotice"))
            .exec(submitForm(config, PRIVACY_SUBMIT_REQUEST, mapOf("agreesToPrivacyNotice" to "true")))
            .exec(
                OneLoginAuthentication.identityVerificationChain(
                    config,
                    "#{registrationNextUrl}",
                    identitySubject = registrationSubject,
                ),
            )
            .exec { session -> session.set("registrationNextUrl", session.getString("identityVerificationNextUrl")) }
            .exec(formPage(config, CONFIRM_IDENTITY_FORM_REQUEST, "registrationNextUrl", "_csrf"))
            .exec(submitForm(config, CONFIRM_IDENTITY_SUBMIT_REQUEST, emptyMap()))
            .exec(formPage(config, EMAIL_FORM_REQUEST, "registrationNextUrl", "emailAddress"))
            .exec(
                submitForm(
                    config,
                    EMAIL_SUBMIT_REQUEST,
                    mapOf("emailAddress" to "synthetic-registration@example.com"),
                ),
            )
            .exec(formPage(config, PHONE_FORM_REQUEST, "registrationNextUrl", "phoneNumber"))
            .exec(submitForm(config, PHONE_SUBMIT_REQUEST, mapOf("phoneNumber" to "02079460123")))
            .exec(formPage(config, LANDLORD_TYPE_FORM_REQUEST, "registrationNextUrl", "landlordType"))
            .exec(submitForm(config, LANDLORD_TYPE_SUBMIT_REQUEST, mapOf("landlordType" to "INDIVIDUAL")))
            .exec(formPage(config, COUNTRY_FORM_REQUEST, "registrationNextUrl", "livesInEnglandOrWales"))
            .exec(submitForm(config, COUNTRY_SUBMIT_REQUEST, mapOf("livesInEnglandOrWales" to "true")))
            .exec(formPage(config, ADDRESS_LOOKUP_FORM_REQUEST, "registrationNextUrl", "postcode"))
            .exec(
                submitForm(
                    config,
                    ADDRESS_LOOKUP_SUBMIT_REQUEST,
                    mapOf(
                        "postcode" to "EG1 2AA",
                        "houseNameOrNumber" to "1",
                    ),
                ),
            )
            .exec(formPage(config, ADDRESS_SELECTION_FORM_REQUEST, "registrationNextUrl", "address"))
            .exec(submitForm(config, ADDRESS_SELECTION_SUBMIT_REQUEST, mapOf("address" to "MANUAL")))
            .exec(formPage(config, MANUAL_ADDRESS_FORM_REQUEST, "registrationNextUrl", "addressLineOne"))
            .exec(
                submitForm(
                    config,
                    MANUAL_ADDRESS_SUBMIT_REQUEST,
                    mapOf(
                        "addressLineOne" to "1 Example Road",
                        "townOrCity" to "Townville",
                        "postcode" to "SW1A 1AA",
                    ),
                ),
            )
            .exec(formPage(config, CHECK_ANSWERS_FORM_REQUEST, "registrationNextUrl", "submittedFilteredJourneyData"))
            .exec(submitForm(config, CHECK_ANSWERS_SUBMIT_REQUEST, emptyMap()))
            .exec(
                http(COMPLETION_REQUEST)
                    .get("#{registrationNextUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(
                        header("Location")
                            .transform { HttpDestination.resolve(it, config.baseUrl) }
                            .saveAs("registrationConfirmationAuthorizationEntry"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http(REAUTHENTICATION_REQUEST)
                    .get("#{registrationConfirmationAuthorizationEntry}")
                    .disableFollowRedirect()
                    .check(status().`is`(302))
                    .check(
                        header("Location")
                            .transform { HttpDestination.resolve(it, config.simulatorUrl) }
                            .saveAs(OneLoginAuthentication.authorizationUrlKey(POST_REGISTRATION_AUTHENTICATION_PREFIX)),
                    ),
            ).exitHereIfFailed()
            .exec(
                OneLoginAuthentication.authenticationCompletionChain(
                    config,
                    registrationSubject,
                    POST_REGISTRATION_AUTHENTICATION_PREFIX,
                ),
            )
            .exec(
                http(CONFIRMATION_REQUEST)
                    .get("#{registrationNextUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                    .check(
                        css(".govuk-panel__body strong")
                            .transform { registrationNumber ->
                                require(registrationNumber.isNotBlank()) {
                                    "Registration confirmation did not include a registration number"
                                }
                                registrationNumber.trim()
                            }.saveAs("registrationNumber"),
                    )
                    .check(
                        css("a[href*='/landlord/dashboard']", "href")
                            .transform { HttpDestination.resolve(it, config.baseUrl) }
                            .saveAs("registrationDashboardUrl"),
                    ),
            ).exitHereIfFailed()
            .exec(
                http(DASHBOARD_REQUEST)
                    .get("#{registrationDashboardUrl}")
                    .disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                    .check(css("h1").exists()),
            ).exitHereIfFailed()

    private fun formPage(
        config: BasicRunConfig,
        requestName: String,
        pageUrlKey: String,
        requiredFieldName: String,
    ): ChainBuilder =
        exec { session ->
            val pageUrl = requireNotNull(session.getString(pageUrlKey)) { "$pageUrlKey is missing" }
            session.set("registrationPageUrl", HttpDestination.resolve(pageUrl, config.baseUrl))
        }.exec(
            http(requestName)
                .get("#{registrationPageUrl}")
                .disableFollowRedirect()
                .check(status().`is`(200))
                .check(header("Content-Type").transform { it.substringBefore(';').trim().lowercase() }.`is`("text/html"))
                .check(css(".govuk-error-summary").notExists())
                .check(css("form[method=post]", "action").saveAs("registrationFormAction"))
                .check(css("form input[type=hidden]", "name").findAll().saveAs("registrationHiddenNames"))
                .check(css("form input[type=hidden]", "value").findAll().saveAs("registrationHiddenValues"))
                .check(css("input[name='$requiredFieldName']").exists()),
        ).exitHereIfFailed()

    private fun submitForm(
        config: BasicRunConfig,
        requestName: String,
        formFields: Map<String, String>,
    ): ChainBuilder =
        exec(
            http(requestName)
                .post { session ->
                    HttpDestination.resolve(
                        requireNotNull(session.getString("registrationFormAction")) { "Registration form action is missing" },
                        config.baseUrl,
                        requireNotNull(session.getString("registrationPageUrl")) { "Registration page URL is missing" },
                    )
                }.disableFollowRedirect()
                .formParamMap { session ->
                    val names = session.getList<String>("registrationHiddenNames")
                    val values = session.getList<String>("registrationHiddenValues")
                    require(names.size == values.size) { "Registration form has hidden fields with missing values" }
                    names.zip(values).toMap() + formFields
                }.check(status().`is`(302))
                .check(
                    header("Location")
                        .transform { HttpDestination.resolve(it, config.baseUrl) }
                        .saveAs("registrationNextUrl"),
                ),
        ).exitHereIfFailed()
}
