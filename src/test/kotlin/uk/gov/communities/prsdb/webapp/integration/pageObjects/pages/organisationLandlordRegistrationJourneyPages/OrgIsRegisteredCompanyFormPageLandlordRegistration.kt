package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCompanyStep

class OrgIsRegisteredCompanyFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgIsRegisteredCompanyStep.ROUTE_SEGMENT}") {
    val form = OrgIsRegisteredCompanyForm(page)

    class OrgIsRegisteredCompanyForm(
        page: Page,
    ) : FormWithSectionHeader(page) {
        val yesLabel: Locator get() = locator.locator("label[for='companiesHouse-yes']")
        val noLabel: Locator get() = locator.locator("label[for='companiesHouse-no']")
    }
}
