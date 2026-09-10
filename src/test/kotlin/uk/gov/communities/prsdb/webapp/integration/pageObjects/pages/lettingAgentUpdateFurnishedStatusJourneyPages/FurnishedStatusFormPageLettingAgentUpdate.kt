package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateFurnishedStatusJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateFurnishedStatusController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FurnishedStatusFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep
import java.util.UUID

class FurnishedStatusFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : FurnishedStatusFormBasePage(
        page,
        LettingAgentUpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${FurnishedStatusStep.ROUTE_SEGMENT}",
    )
