package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.TextInput

abstract class FormBasePage(
    page: Page,
    urlSegment: String,
    val pageHeading: String,
    val inputLabel: String,
) : BasePage(page, urlSegment) {
    val inputFormGroup = fieldsetInput(inputLabel)
    val submitButton = page.locator("button[type=\"submit\"]")

    fun fillInput(text: String) = inputFormGroup.input.fill(text)

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun submit(): Page {
        submitButton.click()
        return page
    }

    val fieldSetHeading = page.locator(".govuk-fieldset__heading")

    val inputFormErrorMessage = page.locator(".govuk-error-message")

    protected fun fieldsetInput(fieldName: String) = TextInput(page.locator(".govuk-fieldset:has(input[name=\"$fieldName\"])"))
}
