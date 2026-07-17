package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateHouseholdsAndTenantsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader.SectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideHouseholdDetailsLaterStep

class ProvideHouseholdDetailsLaterFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        UpdateHouseholdsAndTenantsController.getUpdateHouseholdsAndTenantsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ProvideHouseholdDetailsLaterStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val form = PostForm(page)
    val sectionHeader = SectionHeader(page.locator("main"))
}
