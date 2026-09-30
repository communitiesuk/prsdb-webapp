package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasGasSupplyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasSupplyStep
import java.util.UUID

class HasGasSupplyFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : HasGasSupplyFormBasePage(
        page,
        LettingAgentUpdateGasSafetyController.getUpdateGasSafetyRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${BeforePdjb1022HasGasSupplyStep.ROUTE_SEGMENT}",
    )
