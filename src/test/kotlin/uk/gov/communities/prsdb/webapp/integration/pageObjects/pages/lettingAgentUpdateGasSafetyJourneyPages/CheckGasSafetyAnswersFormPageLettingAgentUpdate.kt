package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckGasSafetyAnswersFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckGasSafetyAnswersStep
import java.util.UUID

class CheckGasSafetyAnswersFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckGasSafetyAnswersFormBasePage(
        page,
        LettingAgentUpdateGasSafetyController.getUpdateGasSafetyRoute(UUID.fromString(urlArguments["token"]!!)) +
            "/${CheckGasSafetyAnswersStep.ROUTE_SEGMENT}",
    )
