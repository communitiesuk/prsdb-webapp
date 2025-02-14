package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class FilterPanel(
    private val page: Page,
    locator: Locator = page.locator(".moj-filter-layout"),
) : BaseComponent(locator) {
    fun getPanel() = getChildComponent(".moj-filter")

    fun getCloseFilterPanelButton() = Button.byText(page, "Close filters panel")

    fun getShowFilterPanel() = Button.byText(page, "Show filters panel")

    val clearFiltersLink = Link.byText(page, "Clear filters")

    fun getSelectedHeadings() = getChildrenComponents(".moj-filter__selected >> h3")

    fun getNoFiltersSelectedText() = getChildComponent(".moj-filter__selected", Locator.LocatorOptions().setHasText("No filters selected"))

    fun clickApplyFiltersButton() = Form(page, parentLocator = locator).submit()

    fun getFilterCheckboxes(label: String? = null) = Form(page, parentLocator = locator).getCheckboxes(label)

    fun getRemoveFilterTag(filterOption: String) = getChildComponent(".moj-filter__tag", Locator.LocatorOptions().setHasText(filterOption))
}
