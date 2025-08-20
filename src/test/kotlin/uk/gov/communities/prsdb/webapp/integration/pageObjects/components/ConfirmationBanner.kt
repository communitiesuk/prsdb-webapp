package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class ConfirmationBanner(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-panel--confirmation")) {
    constructor(page: Page) : this(page.locator("html"))

    val title = Heading(locator.locator(".govuk-panel__title"))
}
