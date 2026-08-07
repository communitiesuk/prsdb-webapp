package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

class OrgIsRegisteredCompanyFormPageUpdateCompaniesHouse(
    page: Page,
) : BasePage(page, "$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}") {
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
