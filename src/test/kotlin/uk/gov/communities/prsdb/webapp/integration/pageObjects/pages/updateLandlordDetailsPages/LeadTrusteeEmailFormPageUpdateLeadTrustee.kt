package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep

class LeadTrusteeEmailFormPageUpdateLeadTrustee(
    page: Page,
) : EmailFormPage(page, "$UPDATE_LEAD_TRUSTEE_ROUTE/${LeadTrusteeEmailStep.ROUTE_SEGMENT}")
