package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage
import kotlin.reflect.KClass

class Pagination<TPage : BasePage>(
    private val page: Page,
    private val pageClass: KClass<TPage>,
    locator: Locator = page.locator(".govuk-pagination"),
) : BaseComponent(locator) {
    fun getPreviousLink() = getLinkWithText("Previous")

    fun getCurrentPageNumberLinkText(): String =
        getChildComponent("a[aria-current='page']")
            .innerText()

    fun getPageNumberLink(pageNumber: Int) = getLinkWithText(pageNumber.toString())

    fun getNextLink() = getLinkWithText("Next")

    fun clickLinkAndAssertNextPage(linkLocator: Locator): TPage = clickChildElementAndAssertNextPage(linkLocator, page, pageClass)

    private fun getLinkWithText(text: String) = getChildComponent(".govuk-pagination__link", Locator.LocatorOptions().setHasText(text))
}
