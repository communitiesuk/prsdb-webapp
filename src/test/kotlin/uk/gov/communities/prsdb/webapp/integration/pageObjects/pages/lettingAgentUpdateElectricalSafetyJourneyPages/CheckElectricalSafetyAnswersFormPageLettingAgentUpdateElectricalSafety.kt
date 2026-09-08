package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateElectricalSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateElectricalSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckElectricalSafetyAnswersFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckElectricalSafetyAnswersStep
import java.util.UUID

class CheckElectricalSafetyAnswersFormPageLettingAgentUpdateElectricalSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckElectricalSafetyAnswersFormBasePage(
        page,
        LettingAgentUpdateElectricalSafetyController.getUpdateElectricalSafetyRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${CheckElectricalSafetyAnswersStep.ROUTE_SEGMENT}",
    )
