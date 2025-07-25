package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.EDIT_USER_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SubHeading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class EditLaUserPage(
    page: Page,
) : BasePage(page, "/$EDIT_USER_PATH_SEGMENT/") {
    val name = Heading.default(page)
    val email = SubHeading(page)
    val form = EditLaUserForm(page)
    val removeAccountButton = Button.byText(page, "Remove this account")

    fun selectManagerRadio() {
        form.isManagerRadios.selectValue("true")
    }

    val isManagerSelected: Boolean
        get() = form.isManagerRadios.selectedValue == "true"

    class EditLaUserForm(
        page: Page,
    ) : PostForm(page) {
        val isManagerRadios = Radios(locator)
    }
}
