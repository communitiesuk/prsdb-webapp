package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills.UpdateRentIncludesBillsCyaStep

class CheckRentIncludesBillsAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        UpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateRentIncludesBillsCyaStep.ROUTE_SEGMENT}",
    )
