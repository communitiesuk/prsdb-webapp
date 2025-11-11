package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Paragraph
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

open class EditLocalCouncilUserBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val name = Heading(page.locator("h1.govuk-fieldset__heading"))
    val email = Paragraph.byText(page, "Email:")
    val form = EditLaUserForm(page)
    val removeAccountButton = Button.byText(page, "Remove this account")

    fun selectManagerRadio() {
        form.isManagerRadios.selectValue("true")
    }

    fun selectNotManagerRadio() {
        form.isManagerRadios.selectValue("false")
    }

    val isManagerSelected: Boolean
        get() = form.isManagerRadios.selectedValue == "true"

    class EditLaUserForm(
        page: Page,
    ) : PostForm(page) {
        val isManagerRadios = Radios(locator)
    }
}
