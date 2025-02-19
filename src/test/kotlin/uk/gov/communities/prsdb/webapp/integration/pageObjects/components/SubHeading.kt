package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class SubHeading(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("main header p")) {
    constructor(page: Page) : this(page.locator("html"))
}
