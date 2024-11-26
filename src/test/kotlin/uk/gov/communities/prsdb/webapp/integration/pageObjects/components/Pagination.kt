package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Pagination(
    private val page: Page,
    locator: Locator = page.locator(".govuk-pagination"),
) : BaseComponent(locator) {
    fun getPreviousLink() = getLinkWithText("Previous")

    fun getCurrentPageNumberLinkText(): String =
        getChildComponent("a[aria-current='page']")
            .innerText()

    fun getPageNumberLink(pageNumber: Int) = getLinkWithText(pageNumber.toString())

    fun getNextLink() = getLinkWithText("Next")

    private fun getLinkWithText(text: String) = getChildComponent(".govuk-pagination__link", Locator.LocatorOptions().setHasText(text))
}
