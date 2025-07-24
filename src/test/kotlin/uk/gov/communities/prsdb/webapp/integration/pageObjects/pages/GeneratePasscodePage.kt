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
    val instructionsText = page.locator("section p.govuk-body")
    val generateAnotherButton = Button.byText(page, "Generate another passcode")
    val returnToDashboardLink = Link.byText(page, "Return to dashboard", selectorOrLocator = "a")

    fun getPasscodeFromBanner(): String {
        return page.locator(".govuk-panel--confirmation strong").textContent() ?: ""
    }

    fun clickGenerateAnother(): GeneratePasscodePage {
        generateAnotherButton.clickAndWait()
        return createValidPage(page, GeneratePasscodePage::class)
    }

    fun clickReturnToDashboard(): LocalAuthorityDashboardPage {
        returnToDashboardLink.clickAndWait()
        return createValidPage(page, LocalAuthorityDashboardPage::class)
    }
}
