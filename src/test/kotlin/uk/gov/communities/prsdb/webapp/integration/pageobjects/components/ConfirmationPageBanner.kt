package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import org.junit.jupiter.api.Assertions.assertTrue

class ConfirmationPageBanner(
    locator: Locator,
) : BaseComponent(locator) {
    fun assertHasMessage(message: String) {
        assertTrue(locator.textContent().contains(message))
    }
}
