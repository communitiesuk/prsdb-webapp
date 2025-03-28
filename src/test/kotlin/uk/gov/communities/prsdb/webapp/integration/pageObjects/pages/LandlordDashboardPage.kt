package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordDashboardController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordDashboardPage(
    page: Page,
) : BasePage(page, LANDLORD_DASHBOARD_URL) {
    val bannerHeading = Heading(page.locator("div.prsd-dashboard-panel h1.govuk-heading-xl"))
    val bannerSubHeading = Heading(page.locator("div.prsd-dashboard-panel div.govuk-body-l"))
    val registerPropertyButton = Button.byText(page, "Register a property")
    val viewPropertyRecordsButton = Button.byText(page, "View your property records")
    val viewLandlordRecordButton = Button.byText(page, "View your landlord record")

    val rentersRightsBillLink = Link.byText(page, "Renters' Rights Bill")
}
