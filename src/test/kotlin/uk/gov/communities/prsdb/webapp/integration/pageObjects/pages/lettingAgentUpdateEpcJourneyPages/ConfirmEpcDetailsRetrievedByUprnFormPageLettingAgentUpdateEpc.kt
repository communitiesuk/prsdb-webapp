package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStep
import java.util.UUID

class ConfirmEpcDetailsRetrievedByUprnFormPageLettingAgentUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        LettingAgentUpdateEpcController.getUpdateEpcRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${ConfirmEpcRetrievedByUprnStep.ROUTE_SEGMENT}",
    )
