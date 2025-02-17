package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Table(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-table")) {
    constructor(page: Page) : this(page.locator("html"))

    fun getHeaderCell(colIndex: Int) = getChildComponent(getHeaderRow(), "th", index = colIndex)

    fun getCell(
        rowIndex: Int,
        colIndex: Int,
    ) = getChildComponent(getRow(rowIndex), "td", index = colIndex)

    fun countRows() = locator.locator("tbody").locator("tr").count()

    private fun getHeaderRow() = getChildComponent("thead tr")

    private fun getRow(index: Int) = getChildComponent("tbody tr", index = index)
}
