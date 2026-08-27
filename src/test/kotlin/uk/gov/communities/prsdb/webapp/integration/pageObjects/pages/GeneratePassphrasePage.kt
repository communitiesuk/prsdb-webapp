package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GeneratePassphrasePage(
    page: Page,
) : BasePage(page, "generate-passphrase") {
    val banner = ConfirmationBanner(page)
    val passphrase: String
        get() = page.locator(".govuk-panel--confirmation strong").textContent()
}
