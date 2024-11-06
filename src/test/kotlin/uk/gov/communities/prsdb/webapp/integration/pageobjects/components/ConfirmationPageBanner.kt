package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

class ConfirmationPageBanner(
    locator: Locator,
) : BaseComponent(locator) {
    fun assertHasMessage(message: String) {
        assertThat(locator).containsText(message)
    }
}
