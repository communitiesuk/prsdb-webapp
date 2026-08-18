package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateLeadTrusteeController.Companion.UPDATE_LEAD_TRUSTEE_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.DateFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep

class LeadTrusteeDobFormPageUpdateLeadTrustee(
    page: Page,
) : DateFormPage(page, "$UPDATE_LEAD_TRUSTEE_ROUTE/${LeadTrusteeDobStep.ROUTE_SEGMENT}")
