package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PhoneNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep

class LeadTrusteePhoneFormPageLandlordRegistration(
    page: Page,
) : PhoneNumberFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${LeadTrusteePhoneStep.ROUTE_SEGMENT}") {
    val pageHeading: Locator? = page.locator("h1 label")
}
