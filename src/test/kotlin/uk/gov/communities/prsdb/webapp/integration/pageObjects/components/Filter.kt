package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Filter(
    private val page: Page,
    locator: Locator = page.locator(".moj-filter-layout"),
) : BaseComponent(locator) {
    fun getFilterPanel() = getChildComponent(".moj-filter")

    fun clickCloseFilterPanel() {
        getButton(page, "Close filters panel").click()
        page.waitForLoadState()
    }

    fun clickShowFilterPanel() {
        getButton(page, "Show filters panel").click()
        page.waitForLoadState()
    }

    fun clickClearFiltersLink() {
        getLink(page, "Clear filters").click()
        page.waitForLoadState()
    }

    fun clickApplyFiltersButton() = Form(page, parentLocator = locator).submit()

    fun getFilterCheckboxes(index: Int) = Form(page, parentLocator = locator).getCheckboxes(index)

    fun clickRemoveFilterTag(filterOption: String) {
        getChildComponent(".moj-filter__tag", Locator.LocatorOptions().setHasText(filterOption)).click()
        page.waitForLoadState()
    }
}
