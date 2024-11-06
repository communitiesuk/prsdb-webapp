package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.BasePage
import kotlin.reflect.KClass

class Pagination<TPage : BasePage>(
    locator: Locator,
    private val pageClass: KClass<TPage>,
) : BaseComponent(locator) {
    fun assertNextIsVisible() {
        assertThat(linkWithText("Next")).isVisible()
    }

    fun assertPreviousIsVisible() {
        assertThat(linkWithText("Previous")).isVisible()
    }

    fun assertPageNumberIsVisible(pageNum: Int) {
        assertThat(linkWithText(pageNum.toString())).isVisible()
    }

    fun assertPageNumberIsCurrent(pageNum: Int) {
        assertThat(linkWithText(pageNum.toString()).locator("..")).hasClass("govuk-pagination__item govuk-pagination__item--current")
    }

    fun clickLink(pageNum: Int): TPage {
        linkWithText(pageNum.toString()).click()
        return BasePage.createValid(locator.page(), pageClass)
    }

    private fun linkWithText(text: String): Locator =
        locator.locator("a.govuk-pagination__link").filter(
            FilterOptions().apply {
                hasText = text
            },
        )
}
