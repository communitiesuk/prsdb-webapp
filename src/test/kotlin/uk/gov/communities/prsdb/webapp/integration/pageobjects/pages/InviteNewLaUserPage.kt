package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class InviteNewLaUserPage(
    page: Page,
) : BasePage(page) {
    private val emailInputFormGroup = inputFormGroup("email")
    private val confirmEmailInputFormGroup = inputFormGroup("confirmEmail")
    private val submitButton = page.locator("button[type=\"submit\"]")

    override fun validate() {
        assertThat(header).containsText("Invite someone from Betelgeuse to the database")
    }

    fun fillEmail(text: String) = emailInputFormGroup.input.fill(text)

    fun fillConfirmEmail(text: String) = confirmEmailInputFormGroup.input.fill(text)

    fun fillBothEmailFields(text: String) {
        fillEmail(text)
        fillConfirmEmail(text)
    }

    fun submit(): InviteNewLaUserSuccessPage {
        submitButton.click()
        return createValid(page, InviteNewLaUserSuccessPage::class)
    }

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun assertConfirmEmailErrorContains(text: String) {
        confirmEmailInputFormGroup.assertErrorMessageContains(text)
    }
}
