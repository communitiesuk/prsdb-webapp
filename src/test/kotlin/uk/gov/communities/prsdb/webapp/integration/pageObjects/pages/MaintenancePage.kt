package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.MaintenanceController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import kotlin.test.assertTrue

class MaintenancePage(
    page: Page,
) : BasePage(page, MaintenanceController.MAINTENANCE_ROUTE) {
    // Static page outside the shared layout, served for both services
    override fun validateTitle() {
        val title = page.title()
        val h1Text = page.locator("h1").textContent().trim()
        assertTrue(
            title.startsWith("$h1Text - ") && title.endsWith(" - GOV.UK"),
            "Page title \"$title\" does not match the format \"$h1Text - <service name> - GOV.UK\"",
        )
    }
}
