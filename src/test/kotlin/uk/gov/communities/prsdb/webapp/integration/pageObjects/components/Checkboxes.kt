package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Page.LocatorOptions

class Checkboxes(
    private val page: Page,
    label: String? = null,
    locator: Locator =
        page
            .locator(".govuk-form-group", LocatorOptions().setHasText(label))
            .locator(".govuk-checkboxes"),
) : BaseComponent(locator) {
    fun getCheckbox(value: String) = getChildComponent("input[value='$value']")

    fun checkCheckbox(value: String) = getCheckbox(value).check()
}
