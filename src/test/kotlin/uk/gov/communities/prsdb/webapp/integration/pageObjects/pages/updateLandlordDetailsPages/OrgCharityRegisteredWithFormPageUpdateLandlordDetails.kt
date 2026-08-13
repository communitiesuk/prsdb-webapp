package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgCharityRegisteredWithFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep

class OrgCharityRegisteredWithFormPageUpdateLandlordDetails(
    page: Page,
) : OrgCharityRegisteredWithFormBasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${OrgCharityRegisteredWithStep.ROUTE_SEGMENT}")
