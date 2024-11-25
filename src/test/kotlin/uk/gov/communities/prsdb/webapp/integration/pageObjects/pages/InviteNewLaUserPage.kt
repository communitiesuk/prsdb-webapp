package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteNewLaUserPage(
    page: Page,
) : BasePage(page, "Invite a new user") {
    val form = Form(page)
    val emailInput = form.getTextInput("email")
    val confirmEmailInput = form.getTextInput("confirmEmail")

    fun fillBothEmailFields(text: String) {
        emailInput.fill(text)
        confirmEmailInput.fill(text)
    }

    fun submitFormAndAssertNextPage(): InviteNewLaUserSuccessPage = clickElementAndAssertNextPage(form.getSubmitButton())

    fun submitInvalidForm() = submitInvalidForm(form)
}
