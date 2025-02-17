package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

interface ClickAndWaitable {
    val locator: Locator

    fun clickAndWait() {
        locator.click()
        locator.page().waitForLoadState()
    }
}
