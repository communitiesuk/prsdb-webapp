package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SelectAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep

class OrgGovBodyMemberSelectAddressFormPageLandlordRegistration(
    page: Page,
) : SelectAddressFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/organisation-governing-body-member-${SelectAddressStep.ROUTE_SEGMENT}",
    ) {
    val heading = page.locator("h1")
}
