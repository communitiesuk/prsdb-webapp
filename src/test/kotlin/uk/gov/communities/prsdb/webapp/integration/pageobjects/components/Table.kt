package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.FilterOptions

open class Table(
    locator: Locator,
) : BaseComponent(locator) {
    fun assertHasHeaderCellWithText(text: String) {
        locator.locator("thead th").filter(FilterOptions().apply { hasText = text })
    }

    fun getCellText(
        rowIndex: Int,
        colIndex: Int,
    ): String =
        locator
            .locator("tbody tr")
            .nth(rowIndex)
            .locator("td")
            .nth(colIndex)
            .textContent()
}
