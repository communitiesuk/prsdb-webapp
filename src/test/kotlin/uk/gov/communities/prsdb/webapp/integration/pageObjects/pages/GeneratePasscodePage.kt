package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GeneratePasscodePage(
    page: Page,
) : BasePage(page, "generate-passcode") {
    val banner = GeneratePasscodeBanner(page)
    val generateAnotherButton = Button.byText(page, "Generate another passcode")
    val returnToDashboardButton = Link.byText(page, "Return to dashboard", selectorOrLocator = "a")

    class GeneratePasscodeBanner(
        private val page: Page,
    ) : ConfirmationBanner(page) {
        val passcode: String
            get() = page.locator(".govuk-panel--confirmation strong").textContent()
    }
}
