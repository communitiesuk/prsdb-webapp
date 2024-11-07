package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

class EmailFormPage(
    page: Page,
) : BasePage(page) {
    private val emailInputFormGroup = fieldsetInput("emailAddress")
    val submitButton = page.locator("button[type=\"submit\"]")

    override fun validate() {
        assertThat(fieldSetHeading).containsText("What is your email address?")
    }

    fun fillEmail(text: String) = emailInputFormGroup.input.fill(text)

    inline fun <reified T : BasePage> submit(): T {
        submitButton.click()
        page.waitForLoadState()
        return BasePage.createValid<T>(page)
    }

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun assertEmailFormErrorContains(error: String) {
        emailInputFormGroup.assertErrorMessageContains(error)
    }
}
