package uk.gov.communities.prsdb.webapp.integration.pageObjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Form(
    private val page: Page,
    locator: Locator = page.locator("form"),
) : BaseComponent(locator) {
    fun getErrorMessage(fieldName: String? = null) =
        getChildComponent(if (fieldName == null) ".govuk-error-message" else "p[id='$fieldName-error']")

    fun getTextInput(fieldName: String? = null): Locator = getChildComponent("input${if (fieldName == null) "" else "[name='$fieldName']"}")

    fun getRadios() = Radios(page)

    fun getFieldsetHeading() = getChildComponent(".govuk-fieldset__heading")

    fun getSelect() = Select(page)

    fun getTextArea() = getChildComponent("textarea")

    fun getCheckboxes() = Checkboxes(page)

    fun getSummaryList() = SummaryList(page)

    fun submit() {
        getSubmitButton().click()
        page.waitForLoadState()
    }

    private fun getSubmitButton() = getChildComponent("button[type='submit']")
}
