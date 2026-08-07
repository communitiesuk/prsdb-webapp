package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.enums.OrgType
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Checkboxes
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader

abstract class OrgTypeFormPage(
    page: Page,
    urlSegment: String,
) : BasePage(page, urlSegment) {
    val form = OrgTypeForm(page)

    fun selectCompany() = form.orgTypeCheckboxes.checkCheckbox(OrgType.COMPANY.toString())

    fun selectCharity() = form.orgTypeCheckboxes.checkCheckbox(OrgType.CHARITY.toString())

    fun selectTrust() = form.orgTypeCheckboxes.checkCheckbox(OrgType.TRUST.toString())

    fun deselectTrust() = form.orgTypeCheckboxes.getCheckbox(OrgType.TRUST.toString()).uncheck()

    fun selectNoneOfThese() = form.orgTypeCheckboxes.checkCheckbox(OrgType.NONE.toString())

    class OrgTypeForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val orgTypeCheckboxes = Checkboxes(locator)
    }
}
