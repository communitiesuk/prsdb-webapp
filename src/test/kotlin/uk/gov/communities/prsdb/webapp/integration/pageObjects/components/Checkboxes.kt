package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Locator.LocatorOptions
import com.microsoft.playwright.Page

class Checkboxes(
    parentLocator: Locator,
    label: String? = null,
) : BaseComponent(
        parentLocator
            .locator(".govuk-form-group", LocatorOptions().setHasText(label))
            .locator(".govuk-checkboxes"),
    ) {
    constructor(page: Page, label: String? = null) : this(page.locator("html"), label)

    fun getCheckbox(value: String): Locator = locator.locator("input[value='$value']")

    fun checkCheckbox(value: String) = getCheckbox(value).check()
}
