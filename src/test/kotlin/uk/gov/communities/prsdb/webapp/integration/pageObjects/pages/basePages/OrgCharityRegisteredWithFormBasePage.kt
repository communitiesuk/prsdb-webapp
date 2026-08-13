package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.CharityRegulator
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithRadios

abstract class OrgCharityRegisteredWithFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val heading: Locator = page.locator("h1")
    val form = FormWithRadios(page)

    fun submitCharityRegisteredWith(charityRegulator: CharityRegulator) {
        form.radios.selectValue(charityRegulator.name)
        form.submit()
    }
}
