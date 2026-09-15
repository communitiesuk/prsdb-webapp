package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class TicketPanel(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".moj-ticket-panel")) {
    constructor(page: Page) : this(page.locator("html"))
}
