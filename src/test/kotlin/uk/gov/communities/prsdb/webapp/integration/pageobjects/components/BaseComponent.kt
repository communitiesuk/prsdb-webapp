package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import org.junit.jupiter.api.Assertions.assertEquals

abstract class BaseComponent(
    protected val locator: Locator,
) {
    init {
        assertLocatorIsValid()
    }

    private fun assertLocatorIsValid() {
        assertEquals(1, locator.count())
    }
}
