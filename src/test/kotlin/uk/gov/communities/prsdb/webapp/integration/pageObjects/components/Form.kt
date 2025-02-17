package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Form(
    parentLocator: Locator,
) : BaseComponent(parentLocator.locator("form")) {
    constructor(page: Page) : this(page.locator("html"))

    fun getErrorMessage(fieldName: String? = null) =
        getChildComponent(if (fieldName == null) ".govuk-error-message" else "p[id='$fieldName-error']")

    fun getTextInput(fieldName: String? = null): Locator = getChildComponent("input${if (fieldName == null) "" else "[name='$fieldName']"}")

    fun getRadios() = Radios(locator)

    fun getFieldsetHeading() = getChildComponent(".govuk-fieldset__heading")

    fun getSelect() = Select(locator)

    fun getTextArea() = getChildComponent("textarea")

    fun getCheckboxes(label: String? = null) = Checkboxes(locator, label)

    fun getSummaryList() = SummaryList(locator)

    fun submit() {
        getSubmitButton().click()
        locator.page().waitForLoadState()
    }

    private fun getSubmitButton() = getChildComponent("button[type='submit']")
}
