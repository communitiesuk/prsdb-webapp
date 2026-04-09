package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class PasscodeLimitExceededPage(
    page: Page,
) : BasePage(page, "generate-passcode") {
    val heading = Heading(page.locator("main h1"))
    val bodyTextOne = page.locator("p.govuk-body").first()
    val bodyTextTwo = page.locator("p.govuk-body").nth(1)
}
