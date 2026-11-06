package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CheckBuilder
import io.gatling.javaapi.core.CoreDsl.bodyString
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.controllers.LandlordDetailsController
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController
import uk.gov.communities.prsdb.webapp.controllers.UpdateGoverningBodyController
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationTypeController
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel
import uk.gov.communities.prsdb.webapp.performance.OrganisationOperationsJourney.page
import uk.gov.communities.prsdb.webapp.performance.OrganisationOperationsJourney.redirect
import uk.gov.communities.prsdb.webapp.performance.OrganisationOperationsJourney.scoped
import uk.gov.communities.prsdb.webapp.performance.OrganisationOperationsJourney.start
import uk.gov.communities.prsdb.webapp.performance.OrganisationOperationsJourney.submit

// Organisation structure updates: lead trustee, governing body, Companies House status and trust status changes.
object OrganisationStructureJourney {
    private const val DETAILS = LandlordDetailsController.LANDLORD_DETAILS_FOR_LANDLORD_ROUTE
    private const val LEAD_TRUSTEE_ROUTE = UpdateLeadTrusteeController.UPDATE_LEAD_TRUSTEE_ROUTE
    private const val GOVERNING_BODY_ROUTE = UpdateGoverningBodyController.UPDATE_GOVERNING_BODY_ROUTE
    private const val COMPANIES_HOUSE_ROUTE = UpdateCompaniesHouseController.UPDATE_COMPANIES_HOUSE_ROUTE
    private const val TYPE_ROUTE = UpdateOrganisationTypeController.UPDATE_ORG_TYPE_ROUTE
    private const val SELECTED_ADDRESS = "1 PRSDB Performance Square, EG1 2AA"
    private const val LEAD_TRUSTEE_PHONE = "02079460125"
    private const val PAGE = OrganisationOperationsJourney.PAGE
    private const val NEXT = OrganisationOperationsJourney.NEXT
    private const val TARGET_TYPES = "orgStructTargetTypes"
    private const val TARGET_DISPLAY = "orgStructTargetDisplay"
    private const val CHECKBOX_NAMES = "orgStructCheckboxNames"
    private const val CHECKBOX_VALUES = "orgStructCheckboxValues"
    private val typeLabels = linkedMapOf("COMPANY" to "Company", "CHARITY" to "Charity", "TRUST" to "Trust", "NONE" to "Other")

    val operations = listOf("lead-trustee", "governing-body", "company-number", "to-company", "to-non-company", "add-trust", "remove-trust")

    private val leadTrusteeStepNames =
        listOf("name", "date of birth", "email", "phone", "address lookup", "address select").flatMap { listOf("$it form", "$it submit") }

    private val memberStepNames =
        listOf("who to provide", "name", "date of birth", "address lookup", "address select").flatMap { listOf("$it form", "$it submit") }

    fun requestNames(operation: String): List<String> {
        val label = label(operation)
        val names =
            when (operation) {
                "lead-trustee" ->
                    listOf("details", "start") + leadTrusteeStepNames.map { "lead trustee $it" } +
                        listOf("check answers", "confirm", "saved details")
                "governing-body" ->
                    listOf("details", "start", "initialise", "member list") + memberStepNames.map { "member $it" } +
                        listOf("updated member list", "member list submit", "check answers", "confirm", "saved details")
                "company-number" ->
                    listOf("details", "start", "registered form", "registered submit", "number form", "number submit") +
                        listOf("check answers", "confirm", "saved details")
                "to-company" ->
                    listOf("details", "start", "registered form", "registered submit", "interruption", "interruption submit") +
                        listOf("number form", "number submit", "check answers", "confirm", "saved details")
                "to-non-company" ->
                    listOf("details", "start", "registered form", "registered submit", "interruption", "interruption submit") +
                        memberStepNames.map { "member $it" } +
                        listOf("updated member list", "member list submit", "check answers", "confirm", "saved details")
                "add-trust" ->
                    listOf("details", "start", "type form", "type submit", "interruption", "interruption submit") +
                        leadTrusteeStepNames.map { "lead trustee $it" } + listOf("check answers", "confirm", "saved details")
                "remove-trust" ->
                    listOf("details", "start", "type form", "type submit", "interruption", "interruption submit") +
                        listOf("check answers", "confirm", "saved details")
                else -> throw IllegalArgumentException("Unknown organisation structure operation: $operation")
            }
        return names.map { "$label: $it" }
    }

    fun leadTrusteeName(fixture: OrganisationFixture) = "Performance Lead Trustee ${fixture.organisationId}"

    fun leadTrusteeEmail(fixture: OrganisationFixture) = "lead-trustee-${fixture.organisationId}@example.com"

    fun memberName(fixture: OrganisationFixture) = "Performance Governing Member ${fixture.organisationId}"

    fun companyNumber(fixture: OrganisationFixture): String = "PF" + String.format("%06d", fixture.organisationId % 1_000_000)

    fun addTrustTypes(currentDisplay: String): List<String> = currentTypes(currentDisplay).filter { it != "TRUST" } + "TRUST"

    fun removeTrustTypes(currentDisplay: String): List<String> =
        currentTypes(currentDisplay).filter { it != "TRUST" }.ifEmpty { listOf("NONE") }

    fun journey(
        config: BasicRunConfig,
        fixture: OrganisationFixture,
        operation: String,
        prefix: String,
    ): ChainBuilder {
        val names = requestNames(operation).map { prefix + it }.iterator()
        val registration = RegistrationNumberDataModel(RegistrationNumberType.LANDLORD, fixture.registrationNumber).toString()
        val chain =
            when (operation) {
                "lead-trustee" -> leadTrustee(config, fixture, registration, names)
                "governing-body" -> governingBody(config, fixture, registration, names)
                "company-number" -> companiesHouse(config, fixture, registration, names, wasCompany = true, toCompany = true)
                "to-company" -> companiesHouse(config, fixture, registration, names, wasCompany = false, toCompany = true)
                "to-non-company" -> companiesHouse(config, fixture, registration, names, wasCompany = true, toCompany = false)
                "add-trust" -> trustStatus(config, fixture, registration, names, addTrust = true)
                "remove-trust" -> trustStatus(config, fixture, registration, names, addTrust = false)
                else -> throw IllegalArgumentException("Unknown organisation structure operation: $operation")
            }
        check(!names.hasNext()) { "Organisation structure request names were not all used" }
        return chain
    }

    private fun leadTrustee(
        config: BasicRunConfig,
        fixture: OrganisationFixture,
        registration: String,
        names: Iterator<String>,
    ): ChainBuilder {
        val name = leadTrusteeName(fixture)
        val route = LEAD_TRUSTEE_ROUTE
        return exec(
            detailsPage(config, names.next(), registration)
                .check(css(LEAD_TRUSTEE_HEADING).exists())
                .check(bodyString().transform { name !in it }.`is`(true))
                .check(css("a[href^='$route/']", "href").saveAs(NEXT)),
        ).exitHereIfFailed()
            .exec(start(config, names.next(), route))
            .exec(leadTrusteeSteps(config, fixture, names, route, "$route/lead-trustee-check-your-answers"))
            .exec(page(config, names.next(), route, emptyList(), listOf(bodyString().transform { name in it }.`is`(true))))
            .exec(submit(config, names.next(), route, DETAILS) { emptyList() })
            .exec(
                detailsPage(config, names.next(), registration)
                    .check(css(LEAD_TRUSTEE_HEADING).exists())
                    .check(bodyString().transform { name in it && leadTrusteeEmail(fixture) in it }.`is`(true)),
            ).exitHereIfFailed()
    }

    private fun governingBody(
        config: BasicRunConfig,
        fixture: OrganisationFixture,
        registration: String,
        names: Iterator<String>,
    ): ChainBuilder {
        val member = memberName(fixture)
        val route = GOVERNING_BODY_ROUTE
        return exec(
            detailsPage(config, names.next(), registration)
                .check(css(GOVERNING_BODY_HEADING).exists())
                .check(bodyString().transform { member !in it }.`is`(true))
                .check(css("a[href^='$route/']", "href").saveAs(NEXT)),
        ).exitHereIfFailed()
            .exec(start(config, names.next(), route))
            // The initialise step loads the stored members into the journey and then redirects to the member list.
            .exec(redirect(config, names.next(), route, "$route/organisation-governing-body-member-list"))
            .exec(page(config, names.next(), route, emptyList(), listOf(addAnotherLink())))
            .exec(followNext(config, route))
            .exec(memberSteps(config, fixture, names, route))
            .exec(memberListAndExit(config, names, route, member, "$route/governing-body-check-your-answers"))
            .exec(page(config, names.next(), route, emptyList(), listOf(bodyString().transform { member in it }.`is`(true))))
            .exec(submit(config, names.next(), route, DETAILS) { emptyList() })
            .exec(
                detailsPage(config, names.next(), registration)
                    .check(css(GOVERNING_BODY_HEADING).exists())
                    .check(bodyString().transform { member in it }.`is`(true)),
            ).exitHereIfFailed()
    }

    private fun companiesHouse(
        config: BasicRunConfig,
        fixture: OrganisationFixture,
        registration: String,
        names: Iterator<String>,
        wasCompany: Boolean,
        toCompany: Boolean,
    ): ChainBuilder {
        val route = COMPANIES_HOUSE_ROUTE
        val number = companyNumber(fixture)
        val member = memberName(fixture)
        var chain =
            exec(
                detailsPage(config, names.next(), registration)
                    .check(css(row(REGISTERED_COMPANY)).transform { it.trim() }.`is`(if (wasCompany) "Yes" else "No"))
                    .check(bodyString().transform { number !in it && member !in it }.`is`(true))
                    .check(css("a[href^='$route/']", "href").saveAs(NEXT)),
            ).exitHereIfFailed()
                .exec(start(config, names.next(), route))
                .exec(page(config, names.next(), route, listOf("companiesHouse")))
        chain =
            if (wasCompany == toCompany) {
                chain.exec(
                    submit(config, names.next(), route, "$route/organisation-company-number") { listOf("companiesHouse" to "true") },
                )
            } else {
                chain
                    .exec(
                        submit(config, names.next(), route, "$route/organisation-companies-house-interruption") {
                            listOf("companiesHouse" to toCompany.toString())
                        },
                    ).exec(page(config, names.next(), route, emptyList()))
                    .exec(
                        submit(
                            config,
                            names.next(),
                            route,
                            if (toCompany) "$route/organisation-company-number" else "$route/organisation-governing-body-who-to-provide",
                        ) { emptyList() },
                    )
            }
        chain =
            if (toCompany) {
                chain
                    .exec(page(config, names.next(), route, listOf("companyNumber")))
                    .exec(submit(config, names.next(), route, "$route/check-answers") { listOf("companyNumber" to number) })
                    .exec(page(config, names.next(), route, emptyList(), listOf(bodyString().transform { number in it }.`is`(true))))
            } else {
                chain
                    .exec(memberSteps(config, fixture, names, route))
                    .exec(memberListAndExit(config, names, route, member, "$route/check-answers"))
                    .exec(page(config, names.next(), route, emptyList(), listOf(bodyString().transform { member in it }.`is`(true))))
            }
        chain = chain.exec(submit(config, names.next(), route, DETAILS) { emptyList() })
        var saved =
            detailsPage(config, names.next(), registration)
                .check(css(row(REGISTERED_COMPANY)).transform { it.trim() }.`is`(if (toCompany) "Yes" else "No"))
        saved =
            if (toCompany) {
                saved
                    .check(css(row(COMPANY_NUMBER)).transform { it.trim() }.`is`(number))
                    .check(css(GOVERNING_BODY_HEADING).notExists())
            } else {
                saved
                    .check(css(row(COMPANY_NUMBER)).notExists())
                    .check(css(GOVERNING_BODY_HEADING).exists())
                    .check(bodyString().transform { member in it }.`is`(true))
            }
        return chain.exec(saved).exitHereIfFailed()
    }

    private fun trustStatus(
        config: BasicRunConfig,
        fixture: OrganisationFixture,
        registration: String,
        names: Iterator<String>,
        addTrust: Boolean,
    ): ChainBuilder {
        val route = TYPE_ROUTE
        val name = leadTrusteeName(fixture)
        val targets: (String) -> List<String> = if (addTrust) this::addTrustTypes else this::removeTrustTypes
        val chain =
            exec(
                detailsPage(config, names.next(), registration)
                    .check(css(row("Organisation type")).transform { ("TRUST" in currentTypes(it)) != addTrust }.`is`(true))
                    .check(css(row("Organisation type")).transform { targets(it) }.saveAs(TARGET_TYPES))
                    .check(css(row("Organisation type")).transform { display(targets(it)) }.saveAs(TARGET_DISPLAY))
                    .check(css(LEAD_TRUSTEE_HEADING).let { if (addTrust) it.notExists() else it.exists() })
                    .check(css("a[href^='$route/']", "href").saveAs(NEXT)),
            ).exitHereIfFailed()
                .exec(start(config, names.next(), route))
                .exec(
                    page(
                        config,
                        names.next(),
                        route,
                        emptyList(),
                        listOf(
                            css("form input[type=checkbox]", "name").findAll().saveAs(CHECKBOX_NAMES),
                            css("form input[type=checkbox]", "value").findAll().saveAs(CHECKBOX_VALUES),
                        ),
                    ),
                ).exec(submit(config, names.next(), route, "$route/organisation-type-trust-interruption") { typeFields(it) })
                .exec(page(config, names.next(), route, emptyList()))
        val answers =
            if (addTrust) {
                chain
                    .exec(submit(config, names.next(), route, "$route/lead-trustee-name") { emptyList() })
                    .exec(leadTrusteeSteps(config, fixture, names, route, "$route/organisation-type-check-your-answers"))
            } else {
                chain.exec(submit(config, names.next(), route, "$route/organisation-type-check-your-answers") { emptyList() })
            }
        return answers
            .exec(
                page(
                    config,
                    names.next(),
                    route,
                    emptyList(),
                    listOf(css(row("Organisation type")).transform { normalise(it) }.isEL("#{$TARGET_DISPLAY}")),
                ),
            ).exec(submit(config, names.next(), route, DETAILS) { emptyList() })
            .exec(
                detailsPage(config, names.next(), registration)
                    .check(css(row("Organisation type")).transform { normalise(it) }.isEL("#{$TARGET_DISPLAY}"))
                    .check(css(LEAD_TRUSTEE_HEADING).let { if (addTrust) it.exists() else it.notExists() })
                    .check(bodyString().transform { (name in it) == addTrust }.`is`(true)),
            ).exitHereIfFailed()
    }

    private fun leadTrusteeSteps(
        config: BasicRunConfig,
        fixture: OrganisationFixture,
        names: Iterator<String>,
        route: String,
        afterAddress: String,
    ): ChainBuilder =
        exec(page(config, names.next(), route, listOf("name")))
            .exec(submit(config, names.next(), route, "$route/lead-trustee-dob") { listOf("name" to leadTrusteeName(fixture)) })
            .exec(page(config, names.next(), route, listOf("day", "month", "year")))
            .exec(
                submit(config, names.next(), route, "$route/lead-trustee-email") {
                    listOf("day" to "1", "month" to "1", "year" to "1980")
                },
            ).exec(page(config, names.next(), route, listOf("emailAddress")))
            .exec(
                submit(config, names.next(), route, "$route/lead-trustee-phone") {
                    listOf("emailAddress" to leadTrusteeEmail(fixture))
                },
            ).exec(page(config, names.next(), route, listOf("phoneNumber")))
            .exec(
                submit(config, names.next(), route, "$route/lead-trustee-address/lookup-address") {
                    listOf("phoneNumber" to LEAD_TRUSTEE_PHONE)
                },
            ).exec(addressSteps(config, names, route, "$route/lead-trustee-address", afterAddress))

    private fun memberSteps(
        config: BasicRunConfig,
        fixture: OrganisationFixture,
        names: Iterator<String>,
        route: String,
    ): ChainBuilder =
        exec(page(config, names.next(), route, listOf("whoToProvide")))
            .exec(
                submit(config, names.next(), route, "$route/organisation-governing-body-member-name") {
                    listOf("whoToProvide" to "TRUSTEE")
                },
            ).exec(page(config, names.next(), route, listOf("name")))
            .exec(
                submit(config, names.next(), route, "$route/organisation-governing-body-member-dob") {
                    listOf("name" to memberName(fixture))
                },
            ).exec(page(config, names.next(), route, listOf("day", "month", "year")))
            .exec(
                submit(config, names.next(), route, "$route/organisation-governing-body-member-address/lookup-address") {
                    listOf("day" to "2", "month" to "2", "year" to "1975")
                },
            ).exec(
                addressSteps(
                    config,
                    names,
                    route,
                    "$route/organisation-governing-body-member-address",
                    "$route/organisation-governing-body-member-list",
                ),
            )

    private fun addressSteps(
        config: BasicRunConfig,
        names: Iterator<String>,
        route: String,
        addressRoute: String,
        afterAddress: String,
    ): ChainBuilder =
        exec(page(config, names.next(), route, listOf("postcode", "houseNameOrNumber")))
            .exec(
                submit(config, names.next(), route, "$addressRoute/select-address") {
                    listOf("postcode" to "EG1 2AA", "houseNameOrNumber" to "1")
                },
            ).exec(
                page(
                    config,
                    names.next(),
                    route,
                    emptyList(),
                    listOf(css("form input[name=address]", "value").findAll().transform { SELECTED_ADDRESS in it }.`is`(true)),
                ),
            ).exec(submit(config, names.next(), route, afterAddress) { listOf("address" to SELECTED_ADDRESS) })

    private fun memberListAndExit(
        config: BasicRunConfig,
        names: Iterator<String>,
        route: String,
        member: String,
        next: String,
    ): ChainBuilder =
        exec(page(config, names.next(), route, emptyList(), listOf(bodyString().transform { member in it }.`is`(true))))
            .exec(submit(config, names.next(), route, next) { emptyList() })

    private fun typeFields(session: Session): List<Pair<String, String>> {
        // Spring binds the checkboxes as indexed list entries, so post each target under its rendered name.
        val checkboxes =
            session
                .getList<String>(CHECKBOX_VALUES)
                .zip(session.getList<String>(CHECKBOX_NAMES))
                .filter { it.second.matches(Regex("""orgTypes\[\d+]""")) }
                .toMap()
        return session.getList<String>(TARGET_TYPES).map {
            requireNotNull(checkboxes[it]) { "Organisation type form has no $it checkbox" } to it
        }
    }

    private fun currentTypes(display: String): List<String> {
        val labels = typeLabels.entries.associate { it.value to it.key }
        return display.split(",").map { it.trim() }.filter { it.isNotEmpty() }.map {
            requireNotNull(labels[it]) { "Unknown organisation type: $it" }
        }.filter { it != "NONE" }
    }

    private fun display(types: List<String>) = types.joinToString(", ") { typeLabels.getValue(it) }

    private fun label(operation: String) = "Organisation structure ($operation)"

    private fun addAnotherLink(): CheckBuilder = css("a.govuk-button--secondary[role=button]", "href").saveAs(NEXT)

    private fun detailsPage(
        config: BasicRunConfig,
        name: String,
        registration: String,
    ) = http(name).get { HttpDestination.withCacheBuster(config.baseUrl + DETAILS) }
        .disableFollowRedirect()
        .check(status().`is`(200))
        .check(css(row("Landlord Registration Number")).transform { it.trim() }.`is`(registration))

    private fun followNext(
        config: BasicRunConfig,
        route: String,
    ): ChainBuilder =
        exec { session ->
            val next =
                HttpDestination.resolve(
                    requireNotNull(session.getString(NEXT)),
                    config.baseUrl,
                    requireNotNull(session.getString(PAGE)),
                )
            session.set(PAGE, scoped(next, config, route))
        }

    private fun normalise(value: String) = value.trim().replace(Regex("\\s+"), " ")

    private fun row(label: String) = ".govuk-summary-list__key:contains('$label') + .govuk-summary-list__value"

    private const val LEAD_TRUSTEE_HEADING = "h2.govuk-heading-m:contains('Lead trustee')"
    private const val GOVERNING_BODY_HEADING = "h2.govuk-heading-m:contains('Governing body')"
    private const val REGISTERED_COMPANY = "Registered with Companies House"
    private const val COMPANY_NUMBER = "Companies House number"
}
