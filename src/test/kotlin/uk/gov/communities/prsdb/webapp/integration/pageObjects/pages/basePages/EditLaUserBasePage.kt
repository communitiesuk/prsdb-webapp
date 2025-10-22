package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.SubHeading

open class EditLaUserBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val name = Heading.default(page)
    val email = SubHeading(page)
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
