package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class LandlordTypeFormBasePage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = LandlordTypeForm(page)

    fun submitIndividual() {
        form.landlordTypeRadios.selectValue("INDIVIDUAL")
        form.submit()
    }

    fun submitOrganisation() {
        form.landlordTypeRadios.selectValue("ORGANISATION")
        form.submit()
    }

    class LandlordTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val landlordTypeRadios = Radios(locator)
    }
}
