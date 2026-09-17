package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class BulletPointList(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("ul.govuk-list--bullet")) {
    constructor(page: Page) : this(page.locator("html"))

    val items: Locator = locator.locator("li")
}
