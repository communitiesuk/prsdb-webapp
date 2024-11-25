package uk.gov.communities.prsdb.webapp.integration.pageobjects.components

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page

class Form(
    private val page: Page,
    locator: Locator = page.locator("form"),
) : BaseComponent(locator) {
    fun getErrorMessage() = getChildComponent(".govuk-error-message")

    fun getTextInput(fieldName: String? = null): Locator = getChildComponent("input${if (fieldName == null) "" else "[name='$fieldName']"}")

    fun getRadios() = Radios(page)

    fun getSubmitButton() = getChildComponent("button[type='submit']")
}
