package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class GeneratePasscodePage(
    page: Page,
) : BasePage(page, "generate-passcode") {
    val backLink = BackLink.default(page)
    val confirmationBanner = ConfirmationBanner(page)
    val heading = Heading(page.locator("main h1"))
    val confirmationPanelHeading = Heading(page.locator(".govuk-panel--confirmation .govuk-panel__title"))
    val generateAnotherButton = Button.byText(page, "Generate another passcode")
    val returnToDashboardLink = Link.byText(page, "Return to dashboard", selectorOrLocator = "a")
    val banner = GeneratePasscodeBanner(page)

    fun clickGenerateAnother() {
        generateAnotherButton.clickAndWait()
    }

    fun clickReturnToDashboard() {
        returnToDashboardLink.clickAndWait()
    }

    class GeneratePasscodeBanner(
        page: Page,
    ) : ConfirmationBanner(page) {
        val passcode = page.locator(".govuk-panel--confirmation strong")
        val instructions = page.locator(".govuk-panel--confirmation p.govuk-body")

        fun getPasscode() = passcode.textContent() ?: ""
    }
}
