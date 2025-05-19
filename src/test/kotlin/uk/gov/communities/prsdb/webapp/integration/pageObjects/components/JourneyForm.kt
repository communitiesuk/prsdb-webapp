package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class JourneyForm(
    parentLocator: Locator,
) : Form(parentLocator) {
    constructor(page: Page) : this(page.locator("html"))

    val csrfToken: String = locator.locator("input[name='_csrf']").inputValue()
}
