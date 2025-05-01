package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Select
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteLaAdminPage(
    page: Page,
) : BasePage(page, "/system-operator/invite-la-admin") {
    val form = InviteLaAdminForm(page)

    fun fillInFormAndSubmit(
        partialLocalAuthorityName: String,
        fullLocalAuthorityName: String,
        email: String,
        confirmEmail: String,
    ) {
        form.localAuthoritySelect.fillPartialAndSelectValue(partialLocalAuthorityName, fullLocalAuthorityName)
        form.emailInput.fill(email)
        form.confirmEmailInput.fill(confirmEmail)
        form.submit()
    }

    class InviteLaAdminForm(
        page: Page,
    ) : Form(page) {
        val localAuthoritySelect = Select(locator)
        val emailInput = TextInput.emailByFieldName(locator, "email")
        val confirmEmailInput = TextInput.emailByFieldName(locator, "confirmEmail")
    }
}
