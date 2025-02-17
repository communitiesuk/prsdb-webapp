package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class FilterPanel(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".moj-filter-layout")) {
    constructor(page: Page) : this(page.locator("html"))

    fun getPanel() = getChildComponent(".moj-filter")

    fun getCloseFilterPanelButton() = Button.byText(locator, "Close filters panel")

    fun getShowFilterPanel() = Button.byText(locator, "Show filters panel")

    val clearFiltersLink = Link.byText(locator, "Clear filters")

    fun getSelectedHeadings() = getChildrenComponents(".moj-filter__selected >> h3")

    fun getNoFiltersSelectedText() = getChildComponent(".moj-filter__selected", Locator.LocatorOptions().setHasText("No filters selected"))

    fun clickApplyFiltersButton() = Form(locator).submit()

    fun getFilterCheckboxes(label: String? = null) = Form(locator).getCheckboxes(label)

    fun getRemoveFilterTag(filterOption: String) = getChildComponent(".moj-filter__tag", Locator.LocatorOptions().setHasText(filterOption))
}
