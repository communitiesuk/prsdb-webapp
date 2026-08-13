package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class OrgCharityNumberFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading: Locator = page.locator("h1")
    val bodyLink: Locator = page.locator("h2.govuk-heading-s + p a.govuk-link")
    val form = CharityNumberForm(page)

    fun submitCharityNumber(charityNumber: String) {
        form.charityNumberInput.fill(charityNumber)
        form.submit()
    }

    class CharityNumberForm(
        page: Page,
    ) : PostForm(page) {
        val charityNumberInput = TextInput.textByFieldName(locator, "charityNumber")
    }
}
