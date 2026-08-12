package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgIsRegisteredCharityFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgIsRegisteredCharityStep

class OrgIsRegisteredCharityFormPageUpdateLandlordDetails(
    page: Page,
) : OrgIsRegisteredCharityFormBasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${OrgIsRegisteredCharityStep.ROUTE_SEGMENT}")
