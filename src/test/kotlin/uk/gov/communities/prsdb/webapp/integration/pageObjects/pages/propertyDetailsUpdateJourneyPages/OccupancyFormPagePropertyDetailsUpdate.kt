package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOccupancyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.OccupancyFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OccupiedStep

class OccupancyFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : OccupancyFormPage(
        page,
        UpdateOccupancyController.getUpdateOccupancyRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${OccupiedStep.ROUTE_SEGMENT}",
    )
