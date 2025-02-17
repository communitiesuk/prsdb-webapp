package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Section(
    locator: Locator,
) : BaseComponent(locator) {
    companion object {
        fun byTestId(
            page: Page,
            testId: String,
        ): Section = Section(page.locator("section[data-testid=\"$testId\"]"))
    }
}
