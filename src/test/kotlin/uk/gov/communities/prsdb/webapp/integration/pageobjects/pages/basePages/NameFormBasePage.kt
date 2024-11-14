package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat

abstract class NameFormBasePage(
    page: Page,
    val pageHeading: String,
) : BasePage(page) {
    private val nameInputFormGroup = fieldsetInput("name")
    val submitButton = page.locator("button[type=\"submit\"]")

    override fun validate() {
        assertThat(fieldSetHeading).containsText(pageHeading)
    }

    fun fillName(text: String) = nameInputFormGroup.input.fill(text)

    abstract fun submit(): BasePage

    fun submitWithoutLoadingPage() {
        submitButton.click()
    }

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun assertNameFormErrorContains(error: String) {
        nameInputFormGroup.assertErrorMessageContains(error)
    }

    fun assertHeadingContains(text: String) {
        assertThat(fieldSetHeading).containsText(text)
    }
}
