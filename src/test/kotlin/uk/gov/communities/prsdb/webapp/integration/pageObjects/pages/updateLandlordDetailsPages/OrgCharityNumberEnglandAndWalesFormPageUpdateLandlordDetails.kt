package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OrgCharityNumberFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep

class OrgCharityNumberEnglandAndWalesFormPageUpdateLandlordDetails(
    page: Page,
) : OrgCharityNumberFormBasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${OrgCharityNumberEnglandAndWalesStep.ROUTE_SEGMENT}")
