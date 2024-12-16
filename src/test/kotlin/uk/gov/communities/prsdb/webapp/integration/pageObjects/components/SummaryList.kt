package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class SummaryList(
    page: Page,
    locator: Locator = page.locator(".govuk-summary-list"),
) : BaseComponent(locator) {
    fun getRowKey(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), "dt")

    fun getRowValue(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), "dd", index = 0)

    fun getRowAction(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), "dd", index = 1)

    fun getRowActionLink(rowIndex: Int) = Companion.getChildComponent(getRowAction(rowIndex), "a")

    private fun getRow(index: Int) = getChildComponent("div", index = index)
}
