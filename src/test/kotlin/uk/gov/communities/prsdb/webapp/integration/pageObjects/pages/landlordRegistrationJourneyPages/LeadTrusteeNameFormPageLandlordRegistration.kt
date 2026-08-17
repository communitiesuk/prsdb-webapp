package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.landlordRegistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.TextFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep

class LeadTrusteeNameFormPageLandlordRegistration(
    page: Page,
) : TextFormPage(page, "$LANDLORD_REGISTRATION_ROUTE/${LeadTrusteeNameStep.ROUTE_SEGMENT}")
