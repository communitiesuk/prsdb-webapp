package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class SummaryList(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator(".govuk-summary-list")) {
    constructor(page: Page) : this(page.locator("html"))

    fun getRowKey(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), ".govuk-summary-list__key")

    fun getRowValue(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), ".govuk-summary-list__value")

    fun getRowAction(rowIndex: Int) = Companion.getChildComponent(getRow(rowIndex), ".govuk-summary-list__actions")

    fun getRowActionLink(rowIndex: Int) = Companion.getChildComponent(getRowAction(rowIndex), "a")

    private fun getRow(index: Int) = getChildComponent(".govuk-summary-list__row", index = index)
}
