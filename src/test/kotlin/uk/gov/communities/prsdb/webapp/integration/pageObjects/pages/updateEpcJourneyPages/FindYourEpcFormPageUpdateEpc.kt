package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcLookupBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep

class FindYourEpcFormPageUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : EpcLookupBasePage(
        page,
        LandlordUpdateEpcController.UPDATE_EPC_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${FindYourEpcStep.ROUTE_SEGMENT}",
    )
