package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.InterruptionPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.occupancy.OccupancyLettingAgentInterruptionStep

class OccupancyLettingAgentInterruptionPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : InterruptionPage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${OccupancyLettingAgentInterruptionStep.ROUTE_SEGMENT}",
    )
