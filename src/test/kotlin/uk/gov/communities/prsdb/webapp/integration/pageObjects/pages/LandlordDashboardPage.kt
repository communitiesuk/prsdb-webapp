package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordDashboardPage(
    page: Page,
) : BasePage(page, LANDLORD_DASHBOARD_URL) {
    val dashboardBannerHeading = Heading(page.locator("div.prsd-dashboard-panel h1.govuk-heading-xl"))
    val dashboardBannerSubHeading = Heading(page.locator("div.prsd-dashboard-panel div.govuk-body-l"))
    val registerPropertyButton = Button.byText(page, "Register a property")
    val viewIncompletePropertiesButton = Button.byText(page, "View incomplete properties")
    val viewPropertyRecordsButton = Button.byText(page, "View your property records")
    val viewLandlordRecordButton = Button.byText(page, "View your landlord record")
    val notificationBanner = NotificationBanner(page)

    val rentersRightsBillLink = Link.byText(page, "Renters' Rights Bill")
}
