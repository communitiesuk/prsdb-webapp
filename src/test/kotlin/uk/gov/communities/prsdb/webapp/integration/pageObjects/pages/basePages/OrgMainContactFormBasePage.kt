package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class OrgMainContactFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OrgMainContactForm(page)
    val pageHeader: Locator? = page.locator("h1")
    val pageText: Locator? = page.locator("label[for='name']")
    val pageEmail: Locator? = page.locator("label[for='emailAddress']")
    val pagePhoneNumber: Locator? = page.locator("label[for='phoneNumber']")

    fun submit(
        name: String,
        email: String,
        phoneNumber: String,
    ) {
        form.nameInput.fill(name)
        form.emailInput.fill(email)
        form.phoneNumberInput.fill(phoneNumber)
        form.submit()
    }

    class OrgMainContactForm(
        page: Page,
    ) : PostForm(page) {
        val nameInput = TextInput.textByFieldName(locator, "name")
        val emailInput = TextInput.emailByFieldName(locator, "emailAddress")
        val phoneNumberInput = TextInput.textByFieldName(locator, "phoneNumber")
    }
}
