package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

class TextArea(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("textarea")) {
    fun fill(text: String) = locator.fill(text)
}
