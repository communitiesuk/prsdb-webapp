package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Table(
    page: Page,
    locator: Locator = page.locator(".govuk-table"),
) : BaseComponent(locator) {
    fun getHeaderCell(colIndex: Int) = Companion.getChildComponent(getHeaderRow(), "th", index = colIndex)

    fun getCell(
        rowIndex: Int,
        colIndex: Int,
    ) = Companion.getChildComponent(getRow(rowIndex), "td", index = colIndex)

    private fun getHeaderRow() = getChildComponent("thead tr")

    private fun getRow(index: Int) = getChildComponent("tbody tr", index = index)
}
