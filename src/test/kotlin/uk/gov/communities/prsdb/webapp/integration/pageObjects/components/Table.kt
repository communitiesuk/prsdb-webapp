package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Table(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-table")) {
    constructor(page: Page) : this(page.locator("html"))

    val headerRow = HeaderRow(locator)
    val rows = TableRows(locator)

    fun getCell(
        rowIndex: Int,
        colIndex: Int,
    ) = rows.getByIndex(rowIndex).getCell(colIndex)

    fun getClickableCell(
        rowIndex: Int,
        colIndex: Int,
    ) = ClickableTableCell(rows.getByIndex(rowIndex).getCell(colIndex))

    class TableRows(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator("tbody tr")) {
        fun count() = locator.count()

        fun getByIndex(rowIndex: Int) = TableRow(locator, rowIndex)
    }

    class TableRow(
        parentLocator: Locator,
        index: Int,
    ) : BaseComponent(parentLocator.nth(index)) {
        fun getCell(colIndex: Int): Locator = locator.locator("td").nth(colIndex)
    }

    class HeaderRow(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator("thead tr")) {
        fun getCell(colIndex: Int): Locator = locator.locator("th").nth(colIndex)
    }

    class ClickableTableCell(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator) {
        val link: Link = Link.default(locator)
    }
}
