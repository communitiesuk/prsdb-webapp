package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class AuthenticatedHeader(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(SELECTOR)) {
    constructor(page: Page) : this(page.locator("html"))

    val oneLoginToggleButton = Button(parentLocator.locator("$SELECTOR .rebranded-cross-service-header__toggle"))
    val oneLoginAccountLink = Link(parentLocator.locator("$SELECTOR a[href='https://home.account.gov.uk/']"))
    val signOutLink = Link(parentLocator.locator("$SELECTOR a:has(.rebranded-one-login-header__nav__link-content--sign-out)"))

    companion object {
        const val SELECTOR = ".rebranded-one-login-header"
    }
}
