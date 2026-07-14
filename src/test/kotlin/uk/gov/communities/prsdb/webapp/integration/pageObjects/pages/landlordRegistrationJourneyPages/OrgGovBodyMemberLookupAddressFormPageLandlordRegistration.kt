package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.LookupAddressFormPage
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep

class OrgGovBodyMemberLookupAddressFormPageLandlordRegistration(
    page: Page,
) : LookupAddressFormPage(
        page,
        "${RegisterLandlordController.LANDLORD_REGISTRATION_ROUTE}/organisation-governing-body-member-${LookupAddressStep.ROUTE_SEGMENT}",
    ) {
    val heading = page.locator("h1")
}
