package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.laUserRegistrationJourneyPages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class NameFormPageLaUserRegistration(
    page: Page,
) : BasePage(page) {
    private val nameInputFormGroup = fieldsetInput("name")
    val submitButton = page.locator("button[type=\"submit\"]")

    fun fillName(text: String) = nameInputFormGroup.input.fill(text)

    override fun validate() {
        assertThat(fieldSetHeading).containsText("What is your full name?")
    }

    fun submit(): EmailFormPageLaUserRegistration {
        submitButton.click()
        return createValid(page, EmailFormPageLaUserRegistration::class)
    }

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun assertNameFormErrorContains(error: String) {
        nameInputFormGroup.assertErrorMessageContains(error)
    }
}
