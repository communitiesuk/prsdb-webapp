package uk.gov.communities.prsdb.webapp.performance

import io.gatling.javaapi.core.ChainBuilder
import io.gatling.javaapi.core.CoreDsl.css
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.http.HttpDsl.http
import io.gatling.javaapi.http.HttpDsl.status
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.constants.enums.RegistrationNumberType
import uk.gov.communities.prsdb.webapp.models.dataModels.RegistrationNumberDataModel

object CouncilReadJourney {
    val basicRequestNames =
        listOf(
            "Council: dashboard",
            "Council search: LRN",
            "Council drilldown: LRN",
            "Council search: PRN",
            "Council drilldown: PRN",
        )

    fun basicChain(
        config: BasicRunConfig,
        fixture: CouncilFixture,
        prefix: String = "",
    ): ChainBuilder = readChain(config, fixture, prefix, basic = true)

    fun chain(
        config: BasicRunConfig,
        fixture: CouncilFixture,
        prefix: String = "",
    ): ChainBuilder = readChain(config, fixture, prefix, basic = false)

    private fun readChain(
        config: BasicRunConfig,
        fixture: CouncilFixture,
        prefix: String,
        basic: Boolean,
    ): ChainBuilder {
        val landlordRegistration =
            RegistrationNumberDataModel(
                RegistrationNumberType.LANDLORD,
                fixture.landlordRegistrationNumber,
            ).toString()
        val propertyRegistration =
            RegistrationNumberDataModel(
                RegistrationNumberType.PROPERTY,
                fixture.propertyRegistrationNumber,
            ).toString()
        val landlord = "/local-council/landlord-details/${fixture.landlordId}"
        val property = "/local-council/property-details/${fixture.propertyId}"

        fun councilUrl(path: String) = "${config.councilBaseUrl}$path"
        var chain =
            exec(
                http("${prefix}Council: dashboard").get(councilUrl("/local-council/dashboard")).disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(css("h1").`is`(fixture.actorName))
                    .check(css("a[href='/local-council/search/landlord']").exists())
                    .check(css("a[href='/local-council/search/property']").exists()),
            ).exitHereIfFailed()
        for ((label, route, term, expectedPath, registration) in listOf(
            Search("LRN", "landlord", landlordRegistration, landlord, landlordRegistration),
            Search("landlord name", "landlord", fixture.landlordName, landlord, landlordRegistration),
            Search("PRN", "property", propertyRegistration, property, propertyRegistration),
            Search("UPRN", "property", fixture.uprn, property, propertyRegistration),
            Search("property address", "property", fixture.address, property, propertyRegistration),
        ).filter { !basic || it.label == "LRN" || it.label == "PRN" }) {
            val nameSearch = label == "landlord name"
            val linkPath = if (nameSearch) "/local-council/landlord-details/" else expectedPath
            chain =
                chain.exec(
                    http("${prefix}Council search: $label").get(councilUrl("/local-council/search/$route"))
                        .queryParam("searchTerm", term).disableFollowRedirect()
                        .check(status().`is`(200))
                        .check(css("table").transform { it.contains(if (nameSearch) fixture.landlordName else registration) }.`is`(true))
                        .check(
                            css("tbody tr").find().transform { row ->
                                requireNotNull(Regex("[LP]-[A-Z2-9]{4}-[A-Z2-9]{4}").find(row)?.value) {
                                    "Search result is missing its registration number"
                                }
                            }.saveAs("councilSearchRegistration"),
                        )
                        .check(
                            css("tbody a[href*='$linkPath']", "href")
                                .transform {
                                    if (nameSearch) {
                                        val resolved = HttpDestination.resolve(it, config.councilBaseUrl)
                                        val path = java.net.URI.create(resolved).path
                                        require(
                                            path.startsWith(linkPath) && path.removePrefix(linkPath).toLongOrNull()?.let { id ->
                                                id > 0
                                            } == true,
                                        )
                                        resolved
                                    } else {
                                        CouncilHttpForms.destination(it, config, expectedPath, baseUrl = config.councilBaseUrl)
                                    }
                                }.saveAs("councilRecordUrl"),
                        ),
                ).exitHereIfFailed()
                    .exec(
                        http("${prefix}Council drilldown: $label").get("#{councilRecordUrl}").disableFollowRedirect()
                            .check(status().`is`(200))
                            .check(
                                css(".govuk-summary-list").findAll()
                                    .transformWithSession { rows, session ->
                                        rows.any { it.contains(session.getString("councilSearchRegistration")!!) }
                                    }.`is`(true),
                            )
                            .check(
                                css(if (route == "landlord") "main" else "h1").transform {
                                    val expected = if (route == "landlord") fixture.landlordName else fixture.address.replace(",", "")
                                    it.replace(",", "").replace(Regex("\\s+"), " ").trim().contains(expected)
                                }.`is`(true),
                            ),
                    ).exitHereIfFailed()
        }
        if (basic) return chain

        val license = fixture.licenseType?.let { LicensingType.entries[it].name } ?: "NO_LICENSING"
        chain =
            chain.exec(
                http("${prefix}Council search: council and licence filters").get(councilUrl("/local-council/search/property"))
                    .queryParam("searchTerm", propertyRegistration).queryParam("restrictToLocalCouncil", "true")
                    .queryParam("restrictToLicenses", license).disableFollowRedirect()
                    .check(status().`is`(200))
                    .check(css("input[name=restrictToLocalCouncil][checked]").exists())
                    .check(css("input[name=restrictToLicenses][checked]", "value").`is`(license))
                    .check(css("table").transform { it.contains(propertyRegistration) }.`is`(true)),
            ).exitHereIfFailed()
                .exec(
                    http("${prefix}Council search: local landlords").get(councilUrl("/local-council/search/landlord"))
                        .queryParam("searchTerm", landlordRegistration).queryParam("restrictToLocalCouncil", "true")
                        .disableFollowRedirect().check(status().`is`(200))
                        .check(css("table").transform { it.contains(landlordRegistration) }.`is`(true)),
                ).exitHereIfFailed()
                .exec(
                    http("${prefix}Council search: pagination first page").get(councilUrl("/local-council/search/property"))
                        .queryParam("searchTerm", fixture.address.substringAfterLast(",").trim().substringBefore(' '))
                        .disableFollowRedirect().check(status().`is`(200))
                        .check(
                            css("a[rel=next]", "href").find()
                                .transform {
                                    CouncilHttpForms.destination(
                                        it,
                                        config,
                                        "/local-council/search/property",
                                        baseUrl = config.councilBaseUrl,
                                    )
                                }
                                .saveAs("councilNextPage"),
                        )
                        .check(css("tbody a", "href").findAll().transform { it.joinToString("|") }.saveAs("councilFirstPageRecords")),
                ).exitHereIfFailed()
                .exec(
                    http("${prefix}Council search: pagination next page").get("#{councilNextPage}").disableFollowRedirect()
                        .check(status().`is`(200))
                        .check(css("a[aria-current=page]").transform { it.trim() }.`is`("2"))
                        .check(
                            css("tbody a", "href").findAll().transform { it.joinToString("|") }.not {
                                it.getString("councilFirstPageRecords")!!
                            },
                        ),
                ).exitHereIfFailed()
        return chain
    }

    private data class Search(
        val label: String,
        val route: String,
        val term: String,
        val expectedPath: String,
        val registration: String,
    )
}
