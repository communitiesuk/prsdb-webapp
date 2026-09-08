package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentIncludesBillsJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentIncludesBillsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BillsIncludedFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import java.util.UUID

class BillsIncludedFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BillsIncludedFormBasePage(
        page,
        LettingAgentUpdateRentIncludesBillsController.getUpdateRentIncludesBillsRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${BillsIncludedStep.ROUTE_SEGMENT}",
    )
