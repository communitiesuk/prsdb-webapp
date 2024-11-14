package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

abstract class PhoneNumberBasePage(
    page: Page,
    val pageHeading: String,
) : BasePage(page) {
    private val phoneNumberFormGroup = fieldsetInput("phoneNumber")
    val submitButton = page.locator("button[type=\"submit\"]")

    override fun validate() {
        assertThat(fieldSetHeading).containsText(pageHeading)
    }

    abstract fun submit(): BasePage

    fun fillPhoneNumber(text: String) = phoneNumberFormGroup.input.fill(text)

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun assertPhoneNumberFormErrorContains(error: String) {
        phoneNumberFormGroup.assertErrorMessageContains(error)
    }
}
