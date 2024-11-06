package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

class TextInput(
    locator: Locator,
) : BaseComponent(locator) {
    val input = locator.locator("input")

    fun assertErrorMessageContains(message: String) {
        assertThat(locator.locator(".govuk-error-message")).containsText(message)
    }
}
