package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import org.junit.jupiter.api.Assertions.assertTrue

class TextInput(
    locator: Locator,
) : BaseComponent(locator) {
    val input = locator.locator("input")

    fun assertErrorMessageContains(message: String) {
        val foundText = locator.locator(".govuk-error-message").textContent()
        assertTrue(foundText.contains(message))
    }
}
