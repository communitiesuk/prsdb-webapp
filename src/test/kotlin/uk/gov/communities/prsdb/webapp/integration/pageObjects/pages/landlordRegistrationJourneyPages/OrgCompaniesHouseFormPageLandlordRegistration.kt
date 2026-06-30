package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Radios
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep

// TODO: PDJB-1138 - Ensure this page matches how the companies house step is implemented
class OrgCompaniesHouseFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgCompaniesHouseStep.ROUTE_SEGMENT}") {
    val form = OrgCompaniesHouseForm(page)

    fun submitYes() {
        form.companiesHouseRadios.selectValue("true")
        form.submit()
    }

    fun submitNo() {
        form.companiesHouseRadios.selectValue("false")
        form.submit()
    }

    class OrgCompaniesHouseForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val companiesHouseRadios = Radios(locator)
    }
}
