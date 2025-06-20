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
    val addComplianceInformationButton = Button.byText(page, "Add compliance information")
    val notificationBanner = DashboardNotificationBanner(page)

    val rentersRightsBillLink = Link.byText(page, "Renters' Rights Bill")

    class DashboardNotificationBanner(
        page: Page,
    ) : NotificationBanner(page) {
        val incompletePropertiesLink =
            Link.byText(
                page,
                "View incomplete properties",
                selectorOrLocator = ".govuk-notification-banner__link",
            )
        val addComplianceInformationLink =
            Link.byText(
                page,
                "Add compliance information",
                selectorOrLocator = ".govuk-notification-banner__link",
            )
    }
}
