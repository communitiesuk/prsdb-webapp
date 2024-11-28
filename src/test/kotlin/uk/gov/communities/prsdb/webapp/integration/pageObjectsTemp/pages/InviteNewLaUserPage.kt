package uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjectsTemp.pages.basePages.BasePage

class InviteNewLaUserPage(
    page: Page,
) : BasePage(page, "/invite-new-user") {
    val form = Form(page)
    val emailInput = form.getTextInput("email")
    val confirmEmailInput = form.getTextInput("confirmEmail")

    fun fillBothEmailFields(text: String) {
        emailInput.fill(text)
        confirmEmailInput.fill(text)
    }
}
