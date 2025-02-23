package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class FormWithSectionHeader(
    page: Page,
) : Form(page) {
    val sectionHeader = SectionHeader(locator)

    class SectionHeader(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator("#section-header"))
}
