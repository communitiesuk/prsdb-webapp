package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateLandlordDetailsPages

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOrganisationLandlordCharityController.Companion.UPDATE_ORG_CHARITY_ROUTE
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.UpdateDetailsTodoStep

class OrgCharityTodoPageUpdateLandlordDetails(
    page: Page,
) : BasePage(page, "$UPDATE_ORG_CHARITY_ROUTE/${UpdateDetailsTodoStep.ROUTE_SEGMENT}") {
    val heading: Locator = page.locator("h1")
}
