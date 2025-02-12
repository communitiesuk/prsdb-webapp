package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions

class FilterPanel(
    private val page: Page,
    locator: Locator = page.locator(".moj-filter-layout"),
) : BaseComponent(locator) {
    fun getPanel(isVisible: Boolean = true) = getChildComponent(".moj-filter", isVisible = isVisible)

    fun getCloseFilterPanelButton() = getButton(page, "Close filters panel")

    fun getShowFilterPanel() = getButton(page, "Show filters panel")

    fun getClearFiltersLink() = getLink(page, "Clear filters")

    fun clickApplyFiltersButton() = Form(page, parentLocator = locator).submit()

    fun getFilterCheckboxes(label: String? = null) = Form(page, parentLocator = locator).getCheckboxes(label)

    fun getRemoveFilterTag(filterOption: String) = getChildComponent(".moj-filter__tag", Locator.LocatorOptions().setHasText(filterOption))
}
