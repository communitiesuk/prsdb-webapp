package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.OwnershipType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class OwnershipTypeFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OwnershipTypeForm(page)
    val backLink = BackLink.default(page)

    fun submitOwnershipType(ownershipType: OwnershipType) {
        form.ownershipTypeRadios.selectValue(ownershipType)
        form.submit()
    }

    class OwnershipTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val ownershipTypeRadios = Radios(locator)
    }
}
