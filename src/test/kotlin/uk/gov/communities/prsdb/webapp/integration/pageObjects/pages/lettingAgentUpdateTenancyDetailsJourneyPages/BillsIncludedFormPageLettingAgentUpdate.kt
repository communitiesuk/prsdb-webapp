package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BillsIncludedFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BillsIncludedStep
import java.util.UUID

class BillsIncludedFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : BillsIncludedFormBasePage(
        page,
        LettingAgentUpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${BillsIncludedStep.ROUTE_SEGMENT}",
    )
