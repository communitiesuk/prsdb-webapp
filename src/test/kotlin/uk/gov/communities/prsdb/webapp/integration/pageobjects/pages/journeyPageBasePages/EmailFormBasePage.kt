package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.journeyPageBasePages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.BasePage

abstract class EmailFormBasePage(
    page: Page,
    val pageHeading: String,
) : BasePage(page) {
    private val emailInputFormGroup = fieldsetInput("emailAddress")
    val submitButton = page.locator("button[type=\"submit\"]")

    override fun validate() {
        assertThat(fieldSetHeading).containsText(pageHeading)
    }

    abstract fun submit(): BasePage

    fun fillEmail(text: String) = emailInputFormGroup.input.fill(text)

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun assertEmailFormErrorContains(error: String) {
        emailInputFormGroup.assertErrorMessageContains(error)
    }
}
