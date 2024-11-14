package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.TextInput

abstract class FormBasePage(
    page: Page,
    val pageHeading: String,
    val inputLabel: String,
) : BasePage(page) {
    val inputFormGroup = fieldsetInput(inputLabel)
    val submitButton = page.locator("button[type=\"submit\"]")

    override fun validate() {
        assertThat(fieldSetHeading).containsText(pageHeading)
    }

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

    protected fun fieldsetInput(fieldName: String) = TextInput(page.locator(".govuk-fieldset:has(input[name=\"$fieldName\"])"))
}
