package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Checkboxes(
    private val page: Page,
    locator: Locator = page.locator(".govuk-checkboxes"),
) : BaseComponent(locator) {
    fun getCheckbox(value: String) = getChildComponent("input[value='$value']")
}
