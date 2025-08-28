package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordController.Companion.LANDLORD_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BetaBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.CookieBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.NotificationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class LandlordDashboardPage(
    page: Page,
) : BasePage(page, LANDLORD_DASHBOARD_URL) {
    val cookieBanner = CookieBanner(page)
    val betaBanner = BetaBanner(page)
    val dashboardBannerHeading = Heading(page.locator("div.prsd-dashboard-panel h1.govuk-heading-xl"))
    val dashboardBannerSubHeading = Heading(page.locator("div.prsd-dashboard-panel div.govuk-body-l"))
    val registerPropertyButton = Button.byText(page, "Register a property")
    val viewIncompletePropertiesButton = Button.byText(page, "Incomplete property details")
    val viewPropertyRecordsButton = Button.byText(page, "View your property records")
    val viewLandlordRecordButton = Button.byText(page, "View your landlord record")
    val addComplianceInformationButton = Button.byText(page, "Compliance actions")
    val notificationBanner = DashboardNotificationBanner(page)

    val rentersRightsBillLink = Link.byText(page, "Renters' Rights Bill")
    val privacyNoticeLink = Link.byText(page, "How your information is used")

    class DashboardNotificationBanner(
        page: Page,
    ) : NotificationBanner(page) {
        val incompletePropertiesLink =
            Link.byText(
                page,
                "View incomplete property details",
                selectorOrLocator = ".govuk-notification-banner__link",
            )
        val addComplianceInformationLink =
            Link.byText(
                page,
                "Compliance actions",
                selectorOrLocator = ".govuk-notification-banner__link",
            )
    }
}
