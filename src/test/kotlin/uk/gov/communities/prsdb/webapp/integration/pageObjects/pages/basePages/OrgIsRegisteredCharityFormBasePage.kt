package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class OrgIsRegisteredCharityFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OrgIsRegisteredCharityForm(page)

    fun submitYes() {
        form.charityRadios.selectValue("true")
        form.submit()
    }

    fun submitNo() {
        form.charityRadios.selectValue("false")
        form.submit()
    }

    class OrgIsRegisteredCharityForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val charityRadios = Radios(locator)
    }
}
