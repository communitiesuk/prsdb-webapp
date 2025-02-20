package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class NameFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = NameForm(page)

    fun submitName(name: String) {
        form.nameInput.fill(name)
        form.submit()
    }

    class NameForm(
        page: Page,
    ) : Form(page) {
        val nameInput = TextInput.textByFieldName(locator, "name")
    }
}
