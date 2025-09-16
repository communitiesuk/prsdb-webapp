package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Paragraph(
    locator: Locator,
) : BaseComponent(locator) {
    companion object {
        fun byTestId(
            page: Page,
            testId: String,
        ) = Paragraph(page.locator("p[data-testid=\"$testId\"]"))
    }
}
