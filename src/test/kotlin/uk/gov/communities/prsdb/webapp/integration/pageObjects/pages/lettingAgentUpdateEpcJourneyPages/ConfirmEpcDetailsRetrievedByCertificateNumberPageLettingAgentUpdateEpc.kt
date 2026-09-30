package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep
import java.util.UUID

class ConfirmEpcDetailsRetrievedByCertificateNumberPageLettingAgentUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        LettingAgentUpdateEpcController.getUpdateEpcRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${ConfirmEpcDetailsRetrievedByCertificateNumberStep.ROUTE_SEGMENT}",
    )
