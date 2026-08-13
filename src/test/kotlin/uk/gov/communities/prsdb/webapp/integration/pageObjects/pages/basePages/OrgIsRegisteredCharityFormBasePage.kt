package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

abstract class OrgIsRegisteredCharityFormBasePage(
    page: Page,
    urlSegment: String,
) : PageWithYesNoRadios(page, urlSegment) {
    val heading: Locator = page.locator("h1")
}
