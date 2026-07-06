package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PhoneNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep

class OrgPhoneNumberFormPageLandlordRegistration(
    page: Page,
) : PhoneNumberFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${OrgPhoneNumberStep.ROUTE_SEGMENT}")
