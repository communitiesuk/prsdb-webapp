package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class FeatureFlagOverrideBanner(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(SELECTOR)) {
    constructor(page: Page) : this(page.locator("html"))

    val manageOverridesLink = Link.byText(parentLocator, "Manage overrides")

    companion object {
        const val SELECTOR = "#feature-flag-override-banner"
    }
}
