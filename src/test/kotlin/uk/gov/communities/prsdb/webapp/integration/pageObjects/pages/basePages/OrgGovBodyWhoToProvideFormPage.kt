package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios

abstract class OrgGovBodyWhoToProvideFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OrgGovBodyWhoToProvideForm(page)

    fun submitWhoToProvide(option: GoverningBodyMemberType) {
        form.radios.selectValue(option)
        form.submit()
    }

    class OrgGovBodyWhoToProvideForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val radios = Radios(locator)
    }
}
