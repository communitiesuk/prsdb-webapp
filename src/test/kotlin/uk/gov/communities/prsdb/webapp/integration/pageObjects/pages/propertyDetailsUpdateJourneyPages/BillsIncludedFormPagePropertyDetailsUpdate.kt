package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BillsIncludedFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep

class BillsIncludedFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BillsIncludedFormBasePage(
        page,
        UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${BillsIncludedStep.ROUTE_SEGMENT}",
    )
