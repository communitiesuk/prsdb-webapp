package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Warning
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationConfirmMissingComplianceStep

class ConfirmMissingComplianceFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ReactivationConfirmMissingComplianceStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1.govuk-heading-l"))
    val warning = Warning.default(page)
    val backLink = BackLink.default(page)
    val gasSectionHeading = sectionHeading(page, "Missing valid gas safety certificate")
    val electricalSectionHeading = sectionHeading(page, "Missing valid electrical safety certificate")
    val epcSectionHeading = sectionHeading(page, "Missing valid EPC")
    val areYouSureLegend = Form.FieldsetLegend(page.locator("html"))

    private fun sectionHeading(
        page: Page,
        text: String,
    ) = Heading(page.locator("h2.govuk-heading-m", Page.LocatorOptions().setHasText(text)))
}
