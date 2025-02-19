package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.LocatorAssertions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

abstract class BaseComponent(
    protected open val locator: Locator,
) {
    companion object {
        fun assertThat(component: BaseComponent): LocatorAssertions = assertThat(component.locator)
    }
}
