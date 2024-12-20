package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class SummaryList(
    page: Page,
    locator: Locator = page.locator(".govuk-summary-list"),
) : BaseComponent(locator) {
    fun getRowKey(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), ".govuk-summary-list__key")

    fun getRowValue(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), ".govuk-summary-list__value")

    fun getRowAction(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), ".govuk-summary-list__actions")

    fun getRowActionLink(rowIndex: Int) = Companion.getChildComponent(getRowAction(rowIndex), "a")

    private fun getRow(index: Int) = getChildComponent(".govuk-summary-list__row", index = index)
}
