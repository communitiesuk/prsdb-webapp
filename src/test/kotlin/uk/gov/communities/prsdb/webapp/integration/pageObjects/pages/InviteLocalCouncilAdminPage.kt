package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ManageLocalCouncilAdminsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Select
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteLocalCouncilAdminPage(
    page: Page,
) : BasePage(page, ManageLocalCouncilAdminsController.INVITE_LOCAL_COUNCIL_ADMIN_ROUTE) {
    val form = InviteLocalCouncilAdminForm(page)

    fun fillInFormAndSubmit(
        partialLocalCouncilName: String,
        fullLocalCouncilName: String,
        email: String,
        confirmEmail: String,
    ) {
        form.localCouncilSelect.fillPartialAndSelectValue(partialLocalCouncilName, fullLocalCouncilName)
        form.emailInput.fill(email)
        form.confirmEmailInput.fill(confirmEmail)
        form.submit()
    }

    class InviteLocalCouncilAdminForm(
        page: Page,
    ) : PostForm(page) {
        val localCouncilSelect = Select(locator)
        val emailInput = TextInput.emailByFieldName(locator, "email")
        val confirmEmailInput = TextInput.emailByFieldName(locator, "confirmEmail")
    }
}
