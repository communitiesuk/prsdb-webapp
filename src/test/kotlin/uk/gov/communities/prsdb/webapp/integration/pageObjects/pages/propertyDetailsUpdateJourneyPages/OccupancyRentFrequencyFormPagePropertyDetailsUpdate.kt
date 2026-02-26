package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentFrequencyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep

class OccupancyRentFrequencyFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentFrequencyFormBasePage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${RentFrequencyStep.ROUTE_SEGMENT}",
    )
