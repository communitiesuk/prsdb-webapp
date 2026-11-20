package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Form
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class FreeholderEmailFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading = Heading(page.locator("h1"))
    val backLink = BackLink.default(page)
    val form = FreeholderEmailForm(page)

    fun submitEmail(email: String) {
        form.emailInput.fill(email)
        form.submit()
    }

    class FreeholderEmailForm(
        page: Page,
    ) : Form(page) {
        val emailInput = TextInput.emailByFieldName(locator, "emailAddress")
    }
}
