package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.INVITE_NEW_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteNewLaUserPage(
    page: Page,
) : BasePage(page, "/$INVITE_NEW_USER_PATH_SEGMENT") {
    val form = InviteNewLaUserForm(page)

    fun submitMatchingEmail(email: String) {
        form.emailInput.fill(email)
        form.confirmEmailInput.fill(email)
        form.submit()
    }

    fun submitMismatchedEmails(
        email: String,
        confirm: String,
    ) {
        form.emailInput.fill(email)
        form.confirmEmailInput.fill(confirm)
        form.submit()
    }

    class InviteNewLaUserForm(
        page: Page,
    ) : PostForm(page) {
        val emailInput = TextInput.emailByFieldName(locator, "email")
        val confirmEmailInput = TextInput.emailByFieldName(locator, "confirmEmail")
    }
}
