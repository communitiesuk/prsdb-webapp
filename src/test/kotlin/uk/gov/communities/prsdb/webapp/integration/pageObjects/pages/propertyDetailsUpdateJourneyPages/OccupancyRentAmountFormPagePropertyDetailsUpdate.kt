package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentAmountFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep

class OccupancyRentAmountFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentAmountFormBasePage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${RentAmountStep.ROUTE_SEGMENT}",
    )
