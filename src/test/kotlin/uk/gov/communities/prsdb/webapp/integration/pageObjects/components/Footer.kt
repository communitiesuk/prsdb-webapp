package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Footer(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(SELECTOR)) {
    constructor(page: Page) : this(page.locator("html"))

    val privacyNoticeLink = Link.byText(parentLocator, "Privacy", selectorOrLocator = ".govuk-footer__link")

    val cookiesLink = Link.byText(parentLocator, "Cookies", selectorOrLocator = ".govuk-footer__link")

    val accessibilityStatementLink = Link.byText(parentLocator, "Accessibility statement", selectorOrLocator = ".govuk-footer__link")

    companion object {
        const val SELECTOR = "footer.govuk-footer"
    }
}
