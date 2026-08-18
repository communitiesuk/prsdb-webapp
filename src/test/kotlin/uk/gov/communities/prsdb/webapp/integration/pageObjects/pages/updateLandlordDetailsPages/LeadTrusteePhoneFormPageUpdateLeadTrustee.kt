package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PhoneNumberFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep

class LeadTrusteePhoneFormPageUpdateLeadTrustee(
    page: Page,
) : PhoneNumberFormPage(page, "$UPDATE_LEAD_TRUSTEE_ROUTE/${LeadTrusteePhoneStep.ROUTE_SEGMENT}")
