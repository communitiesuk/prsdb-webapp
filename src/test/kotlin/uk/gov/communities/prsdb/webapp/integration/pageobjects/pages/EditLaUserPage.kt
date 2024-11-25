package uk.gov.communities.prsdb.webapp.integration.pageobjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getButton
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getHeading
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.BaseComponent.Companion.getSubHeading
import uk.gov.communities.prsdb.webapp.integration.pageobjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageobjects.pages.basePages.BasePage

class EditLaUserPage(
    page: Page,
) : BasePage(page, "Manage Local Authority Users") {
    val name = getHeading(page)
    val email = getSubHeading(page)
    private val form = Form(page)
    val isManagerRadios = form.getRadios()
    private val removeAccountButton = getButton(page, "Remove this account")

    fun submitFormAndAssertNextPage(): ManageLaUsersPage = clickElementAndAssertNextPage(form.getSubmitButton())

    fun clickRemoveAccountButtonAndAssertNextPage(): ConfirmDeleteLaUserPage = clickElementAndAssertNextPage(removeAccountButton)
}
