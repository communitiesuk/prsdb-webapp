package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.TextFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep

class LeadTrusteeNameFormPageUpdateLeadTrustee(
    page: Page,
) : TextFormPage(page, "$UPDATE_LEAD_TRUSTEE_ROUTE/${LeadTrusteeNameStep.ROUTE_SEGMENT}")
