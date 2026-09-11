package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.UpdateEpcCheckYourAnswersBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.epc.UpdateCheckEpcAnswersStep
import java.util.UUID

class CheckEpcAnswersFormPageLettingAgentUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : UpdateEpcCheckYourAnswersBasePage(
        page,
        LettingAgentUpdateEpcController.getUpdateEpcRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${UpdateCheckEpcAnswersStep.ROUTE_SEGMENT}",
    )
