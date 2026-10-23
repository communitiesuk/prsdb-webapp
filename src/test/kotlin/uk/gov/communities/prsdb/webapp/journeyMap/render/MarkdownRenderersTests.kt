package uk.gov.communities.prsdb.webapp.journeyMap.render

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyInfo
import uk.gov.communities.prsdb.webapp.journeyMap.model.JourneyMapManifest
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestArrow
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestCondition
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestDependency
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestError
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestGap
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestNode
import uk.gov.communities.prsdb.webapp.journeyMap.model.ManifestVariant

class MarkdownRenderersTests {
    private val manifest =
        JourneyMapManifest(
            journey = JourneyInfo("test", "Test journey", "/test", "abc1234", "2026-10-21T07:00:00Z", mapOf("flag-a" to true)),
            containers = emptyList(),
            nodes =
                listOf(
                    ManifestNode(
                        "landlord-type",
                        ManifestNode.PAGE,
                        "landlord-type",
                        "Are you registering as an individual or an organisation?",
                        "LandlordTypeStepConfig",
                        emptyList(),
                        listOf(
                            ManifestVariant("v1", "img/a/v1.jpg", "img/a/v1.thumb.jpg", null, emptyList(), ManifestNode.ORIGIN_MAIN),
                            ManifestVariant(
                                "v2",
                                "img/a/v2.jpg",
                                "img/a/v2.thumb.jpg",
                                null,
                                listOf(ManifestCondition("What is your name?", "Verified by One Login")),
                                ManifestNode.ORIGIN_MAIN,
                            ),
                        ),
                        skippedVariants = 3,
                    ),
                    ManifestNode(
                        "organisation-name",
                        ManifestNode.PAGE,
                        "organisation-name",
                        "Organisation name",
                        null,
                        emptyList(),
                        emptyList(),
                    ),
                ),
            edges = emptyList(),
            gaps = listOf(ManifestGap("organisation-name", null, "never visited")),
            errors = listOf(ManifestError("email", "http-500", "Server error")),
            dependencies = listOf(ManifestDependency("internal:Occupied", listOf("occupancy"))),
            arrows =
                listOf(
                    ManifestArrow("landlord-type", "INDIVIDUAL", "country-of-residence", walked = true),
                    ManifestArrow("landlord-type", "ORGANISATION", "organisation-name", walked = false),
                    ManifestArrow("internal:Occupied", "NO", null, walked = true, reads = listOf("step:occupied")),
                ),
        )

    @Test
    fun `the report lists the earlier answers each decision reads`() {
        val report = ReportRenderer().render(manifest)

        assertTrue(report.contains("| internal:Occupied | occupancy | no |"), report)
    }

    @Test
    fun `the report counts the arrows in the routing code and lists those not walked and those depending on earlier answers`() {
        val report = ReportRenderer().render(manifest)

        assertTrue(report.contains("| 3 | 2 | 1 | 1 |"), report)
        assertTrue(report.contains("| landlord-type | ORGANISATION | organisation-name |"), report)
        assertTrue(report.contains("| internal:Occupied | NO | yes | step:occupied |"), report)
    }

    @Test
    fun `the readme embeds the mermaid diagram and lists each page with its variants and conditions`() {
        val readme = ReadmeRenderer().render(manifest)

        assertTrue(readme.contains("```mermaid\nflowchart TD"), readme)
        assertTrue(readme.contains("### Are you registering as an individual or an organisation?"), readme)
        assertTrue(readme.contains("[![v2](img/a/v2.thumb.jpg)](img/a/v2.jpg)"), readme)
        assertTrue(readme.contains("What is your name?: Verified by One Login"), readme)
        assertTrue(readme.contains("Not reached by the generator"), readme)
    }

    @Test
    fun `the report counts pages, variants, gaps and errors and lists each gap and error`() {
        val report = ReportRenderer().render(manifest)

        assertTrue(report.contains("| 2 | 2 | 0 | 1 | 1 |"), report)
        assertTrue(report.contains("| organisation-name |  | never visited |"), report)
        assertTrue(report.contains("| email | http-500 | Server error |"), report)
    }

    @Test
    fun `the report lists pages that had more variants than were captured`() {
        val report = ReportRenderer().render(manifest)

        assertTrue(report.contains("| landlord-type | 3 |"), report)
    }
}
