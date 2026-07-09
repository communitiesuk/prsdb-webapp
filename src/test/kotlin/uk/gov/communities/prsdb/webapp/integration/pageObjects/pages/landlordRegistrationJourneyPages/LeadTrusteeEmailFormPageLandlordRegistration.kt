package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep

class LeadTrusteeEmailFormPageLandlordRegistration(
    page: Page,
) : EmailFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${LeadTrusteeEmailStep.ROUTE_SEGMENT}")
