package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LocalAuthorityDashboardController.Companion.LOCAL_AUTHORITY_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LocalAuthorityDashboardPage(
    page: Page,
) : BasePage(page, LOCAL_AUTHORITY_DASHBOARD_URL) {
    val bannerHeading = Heading(page.locator("div.prsd-dashboard-panel h1.govuk-heading-xl"))
    val bannerSubHeading = Heading(page.locator("div.prsd-dashboard-panel div.govuk-body-l"))
    val manageUsersButton = Link.byText(page, "Manage users", selectorOrLocator = "li.govuk-service-navigation__item")

    val searchPropertyButton = Button.byText(page, "Search for a property")
    val searchLandlordButton = Button.byText(page, "Search for a landlord")
    val privacyNoticeLink = Link.byText(page, "Privacy notice")
    val rentersRightsBillLink = Link.byText(page, "Renters' Rights Bill")
    val aboutPilotLink = Link.byText(page, "About this pilot")
}
