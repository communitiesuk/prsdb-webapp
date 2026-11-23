package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class PropertyManagerDetailsFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = PropertyManagerDetailsForm(page)

    fun submitDetails(
        name: String,
        email: String,
    ) {
        form.nameInput.fill(name)
        form.emailInput.fill(email)
        form.submit()
    }

    class PropertyManagerDetailsForm(
        page: Page,
    ) : PostForm(page) {
        val nameInput = TextInput.textByFieldName(locator, "name")
        val emailInput = TextInput.emailByFieldName(locator, "emailAddress")
    }
}
