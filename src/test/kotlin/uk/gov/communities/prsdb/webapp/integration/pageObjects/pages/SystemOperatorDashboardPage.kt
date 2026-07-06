package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.SystemOperatorDashboardController.Companion.SYSTEM_OPERATOR_DASHBOARD_URL
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class SystemOperatorDashboardPage(
    page: Page,
) : BasePage(page, SYSTEM_OPERATOR_DASHBOARD_URL) {
    val bannerHeading = Heading(page.locator("div.prsd-dashboard-panel h1.govuk-heading-xl"))
    val generatePasscodeButton = Button.byText(page, "Generate a passcode")
    val inviteLocalCouncilAdminButton = Button.byText(page, "Invite a local council admin")
    val metricsButton = Button.byText(page, "View metrics")
}
