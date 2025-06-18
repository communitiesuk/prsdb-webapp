package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator

class UnorderedList(
    locator: Locator,
) : BaseComponent(locator.locator("ul")) {
    val elements = locator.locator("li").all().map { UnorderedListElement(it) }

    fun getElementByTextOrNull(text: String) = elements.find { it.getText() == text }

    class UnorderedListElement(
        locator: Locator,
    ) : BaseComponent(locator) {
        fun getText(): String = locator.textContent()
    }
}
