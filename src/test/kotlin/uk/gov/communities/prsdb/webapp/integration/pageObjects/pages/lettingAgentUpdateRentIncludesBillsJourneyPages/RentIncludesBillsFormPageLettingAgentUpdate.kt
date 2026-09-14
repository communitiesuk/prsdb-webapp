package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentIncludesBillsJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentIncludesBillsFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentIncludesBillsStep
import java.util.UUID

class RentIncludesBillsFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentIncludesBillsFormBasePage(
        page,
        LettingAgentUpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${RentIncludesBillsStep.ROUTE_SEGMENT}",
    )
