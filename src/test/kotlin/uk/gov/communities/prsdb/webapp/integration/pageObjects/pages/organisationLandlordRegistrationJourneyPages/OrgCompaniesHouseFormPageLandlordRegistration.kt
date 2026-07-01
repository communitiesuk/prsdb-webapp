package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep

class OrgCompaniesHouseFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgCompaniesHouseStep.ROUTE_SEGMENT}") {
    val form = OrgCompaniesHouseForm(page)

    class OrgCompaniesHouseForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val yesLabel: Locator get() = locator.locator("label[for='companiesHouse-yes']")
        val noLabel: Locator get() = locator.locator("label[for='companiesHouse-no']")
    }
}
