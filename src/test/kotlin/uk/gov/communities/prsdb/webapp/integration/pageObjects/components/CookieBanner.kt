package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class CookieBanner(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(SELECTOR)) {
    constructor(page: Page) : this(page.locator("html"))

    val acceptCookiesButton = Button.byText(parentLocator, "Accept analytics cookies")
    val rejectCookiesButton = Button.byText(parentLocator, "Reject analytics cookies")
    val viewCookiesLink = Link.byText(parentLocator, "View cookies")

    val changeCookiesSettingLink = Link.byText(parentLocator, "change your cookie settings")
    val hideConfirmationButton = Button.byText(parentLocator, "Hide cookie message")

    companion object {
        const val SELECTOR = ".govuk-cookie-banner"
    }
}
