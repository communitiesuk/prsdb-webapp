package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Pagination(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-pagination").first()) {
    constructor(page: Page) : this(page.locator("html"))

    val previousLink = getLinkWithText("Previous")

    val currentPageNumberLinkText: String
        get() = locator.locator("a[aria-current='page']").innerText()

    fun getPageNumberLink(pageNumber: Int) = getLinkWithText(pageNumber.toString())

    val nextLink = getLinkWithText("Next")

    private fun getLinkWithText(text: String) =
        PaginationLink(locator.locator(".govuk-pagination__link", Locator.LocatorOptions().setHasText(text)))

    class PaginationLink(
        override val locator: Locator,
    ) : BaseComponent(locator),
        ClickAndWaitable
}
