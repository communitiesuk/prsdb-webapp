package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfBedroomsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep

class OccupancyNumberOfBedroomsFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : NumberOfBedroomsFormBasePage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${BedroomsStep.ROUTE_SEGMENT}",
    )
