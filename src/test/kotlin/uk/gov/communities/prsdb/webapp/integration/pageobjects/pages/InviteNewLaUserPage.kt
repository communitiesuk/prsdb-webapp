package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Assertions.assertEquals

class InviteNewLaUserPage(
    page: Page,
) : BasePage(page) {
    private val emailInputFormGroup = inputFormGroup("email")
    private val confirmEmailInputFormGroup = inputFormGroup("confirmEmail")
    private val submitButton = page.locator("button[type=\"submit\"]")

    override fun validate() {
        assertEquals("Invite someone from Betelgeuse to the database", header.textContent())
    }

    fun fillEmail(text: String) = emailInputFormGroup.input.fill(text)

    fun fillConfirmEmail(text: String) = confirmEmailInputFormGroup.input.fill(text)

    fun fillBothEmailFields(text: String) {
        fillEmail(text)
        fillConfirmEmail(text)
    }

    fun submit(): InviteNewLaUserSuccessPage {
        submitButton.click()
        page.waitForLoadState()
        return createValid<InviteNewLaUserSuccessPage>(page)
    }

    fun submitUnsuccessfully() {
        submitButton.click()
        page.waitForLoadState()
    }

    fun assertConfirmEmailErrorContains(text: String) {
        confirmEmailInputFormGroup.assertErrorMessageContains(text)
    }
}
