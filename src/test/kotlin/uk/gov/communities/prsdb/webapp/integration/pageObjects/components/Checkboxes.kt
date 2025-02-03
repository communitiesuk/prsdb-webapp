package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Checkboxes(
    private val page: Page,
    index: Int = 0,
    locator: Locator = page.locator(".govuk-checkboxes").nth(index),
) : BaseComponent(locator) {
    fun getCheckbox(value: String) = getChildComponent("input[value='$value']")

    fun checkCheckbox(value: String) = getCheckbox(value).check()
}
