package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Heading(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("main header h1")) {
    constructor(page: Page) : this(page.locator("html"))
}
