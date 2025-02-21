package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput

abstract class LicenceNumberFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = LicenceNumberForm(page)

    fun submitLicenseNumber(licenceNumber: String) {
        form.licenceNumberInput.fill(licenceNumber)
        form.submit()
    }

    class LicenceNumberForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val licenceNumberInput = TextInput.textByFieldName(locator, "licenceNumber")
    }
}
