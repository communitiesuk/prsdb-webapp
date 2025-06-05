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
    ) = rows.getByIndex(rowIndex).getClickableCell(colIndex)

    class TableRows(
        parentLocator: Locator,
    ) : BaseComponent(parentLocator.locator("tbody tr")) {
        fun count() = locator.count()

        fun getByIndex(rowIndex: Int) = TableRow.atIndex(locator, rowIndex)
    }

    class TableRow(
        locator: Locator,
    ) : BaseComponent(locator) {
        fun getCell(colIndex: Int) = TableCell.atColIndex(locator, colIndex)

        fun getClickableCell(colIndex: Int) = ClickableTableCell.atColIndex(locator, colIndex)

        companion object {
            fun atIndex(
                parentLocator: Locator,
                index: Int,
            ): TableRow = TableRow(parentLocator.nth(index))
        }
    }

    class HeaderRow(
        locator: Locator,
    ) : BaseComponent(locator) {
        fun getCell(colIndex: Int): TableCell = TableCell.headerAtColIndex(locator, colIndex)

        companion object {
            fun default(parentLocator: Locator): HeaderRow = HeaderRow(parentLocator.locator("thead tr"))
        }
    }

    open class TableCell(
        override val locator: Locator,
    ) : BaseComponent(locator) {
        val text: String
            get() = locator.innerText()

        companion object {
            fun atColIndex(
                locator: Locator,
                index: Int,
            ): TableCell = TableCell(locator.locator("td").nth(index))

            fun headerAtColIndex(
                locator: Locator,
                index: Int,
            ): TableCell = TableCell(locator.locator("th").nth(index))
        }
    }

    class ClickableTableCell(
        parentLocator: Locator,
    ) : TableCell(parentLocator) {
        val link: Link = Link.default(locator)

        companion object {
            fun atColIndex(
                locator: Locator,
                index: Int,
            ): ClickableTableCell = ClickableTableCell(locator.locator("td").nth(index))
        }
    }
}
