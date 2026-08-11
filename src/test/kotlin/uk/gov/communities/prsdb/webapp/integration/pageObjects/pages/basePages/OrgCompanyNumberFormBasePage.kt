package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class OrgCompanyNumberFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OrgCompanyNumberForm(page)

    val heading = Heading(page.locator("h1"))

    val hint: Locator = page.locator(".govuk-hint")

    val detailsHeading: Locator = page.locator("h2.govuk-heading-s")

    fun submitCompanyNumber(companyNumber: String) {
        form.companyNumberInput.fill(companyNumber)
        form.submit()
    }

    class OrgCompanyNumberForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val companyNumberInput = TextInput.textByFieldName(locator, "companyNumber")
    }
}
