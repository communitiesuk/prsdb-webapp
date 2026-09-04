package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentIncludesBillsJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentIncludesBills.UpdateRentIncludesBillsCyaStep
import java.util.UUID

class CheckRentIncludesBillsAnswersPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        LettingAgentUpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${UpdateRentIncludesBillsCyaStep.ROUTE_SEGMENT}",
    )
