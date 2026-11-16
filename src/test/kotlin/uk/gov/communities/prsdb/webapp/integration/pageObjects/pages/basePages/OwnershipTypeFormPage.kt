package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipTypeBeforePdjb955
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

// TODO PDJB-2016: delete this page object once the ASSOCIATED_PARTIES flag is always on / deleted -
//  it drives the legacy single-radio ownership type page
abstract class OwnershipTypeFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OwnershipTypeForm(page)

    fun submitOwnershipType(ownershipType: OwnershipTypeBeforePdjb955) {
        form.ownershipTypeRadios.selectValue(ownershipType)
        form.submit()
    }

    class OwnershipTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val ownershipTypeRadios = Radios(locator)
    }
}
