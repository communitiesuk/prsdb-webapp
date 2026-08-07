package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateCompaniesHousePages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateCompaniesHouseController.Companion.UPDATE_COMPANIES_HOUSE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep

class OrgCompanyNumberFormPageUpdateCompaniesHouse(
    page: Page,
) : BasePage(page, "$UPDATE_COMPANIES_HOUSE_ROUTE/${OrgCompanyNumberStep.ROUTE_SEGMENT}") {
    val form = OrgCompanyNumberForm(page)

    fun submitCompanyNumber(companyNumber: String) {
        form.companyNumberInput.fill(companyNumber)
        form.submit()
    }

    class OrgCompanyNumberForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val companyNumberInput = TextInput.textByFieldName(locator, "companyNumber")
    }
}
