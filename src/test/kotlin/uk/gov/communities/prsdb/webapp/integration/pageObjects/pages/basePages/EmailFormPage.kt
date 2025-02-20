package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class EmailFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = EmailForm(page)

    fun submitEmail(email: String) {
        form.emailInput.fill(email)
        form.submit()
    }

    class EmailForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val emailInput = TextInput.emailByFieldName(locator, "emailAddress")
    }
}
