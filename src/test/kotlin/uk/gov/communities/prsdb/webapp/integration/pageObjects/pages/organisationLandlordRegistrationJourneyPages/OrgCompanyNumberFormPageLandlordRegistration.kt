package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.organisationLandlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.FormWithSectionHeader
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.TextInput
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep

class OrgCompanyNumberFormPageLandlordRegistration(
    page: Page,
) : BasePage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgCompanyNumberStep.ROUTE_SEGMENT}") {
    val form = OrgCompanyNumberForm(page)

    val heading = Heading(page.locator("h1"))

    val hint: Locator = page.locator(".govuk-hint")

    val detailsHeading: Locator = page.locator("h2.govuk-heading-s")

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
