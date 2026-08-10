package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class OrgIsRegisteredCompanyFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OrgIsRegisteredCompanyForm(page)

    fun submitYes() {
        form.companiesHouseRadios.selectValue("true")
        form.submit()
    }

    fun submitNo() {
        form.companiesHouseRadios.selectValue("false")
        form.submit()
    }

    class OrgIsRegisteredCompanyForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val companiesHouseRadios = Radios(locator)
    }
}
