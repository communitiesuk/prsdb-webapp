package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateBedroomsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.NumberOfBedroomsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep

class NumberOfBedroomsFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : NumberOfBedroomsFormBasePage(
        page,
        UpdateBedroomsController.getUpdateBedroomsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${BedroomsStep.ROUTE_SEGMENT}",
    )
