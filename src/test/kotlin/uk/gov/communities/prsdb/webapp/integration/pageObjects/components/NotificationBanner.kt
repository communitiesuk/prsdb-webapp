package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

open class NotificationBanner(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-notification-banner")) {
    constructor(page: Page) : this(page.locator("html"))

    val title = Title(locator)

    val content = Content(locator)

    class Title(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-notification-banner__title"))

    class Content(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-notification-banner__content")) {
        val heading = BannerHeading(parentLocator)
    }

    class BannerHeading(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator(".govuk-notification-banner__heading"))
}
