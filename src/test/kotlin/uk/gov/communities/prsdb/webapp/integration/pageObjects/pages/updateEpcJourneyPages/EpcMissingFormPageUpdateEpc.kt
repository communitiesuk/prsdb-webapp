package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep

class EpcMissingFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LandlordUpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${EpcMissingStep.ROUTE_SEGMENT}",
    ) {
    val form = PostForm(page)
}
