package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentIncludesBillsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep

class RentIncludesBillsFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentIncludesBillsFormBasePage(
        page,
        UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${RentIncludesBillsStep.ROUTE_SEGMENT}",
    )
