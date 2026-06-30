package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class TextFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = TextForm(page)

    fun submitName(name: String) {
        form.nameInput.fill(name)
        form.submit()
    }

    class TextForm(
        page: Page,
    ) : PostForm(page) {
        val nameInput = TextInput.textByFieldName(locator, "name")
    }
}
