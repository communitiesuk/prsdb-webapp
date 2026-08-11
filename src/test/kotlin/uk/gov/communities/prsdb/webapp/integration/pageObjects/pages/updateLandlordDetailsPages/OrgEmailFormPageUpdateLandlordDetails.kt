package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordEmailController.Companion.UPDATE_ORG_EMAIL_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EmailFormPage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep

class OrgEmailFormPageUpdateLandlordDetails(
    page: Page,
) : EmailFormPage(page, "$UPDATE_ORG_EMAIL_ROUTE/${OrgEmailStep.ROUTE_SEGMENT}")
