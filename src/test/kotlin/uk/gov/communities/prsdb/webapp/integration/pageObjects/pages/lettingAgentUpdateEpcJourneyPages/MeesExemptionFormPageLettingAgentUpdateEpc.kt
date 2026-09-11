package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.MeesExemptionReasonBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import java.util.UUID

class MeesExemptionFormPageLettingAgentUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : MeesExemptionReasonBasePage(
        page,
        LettingAgentUpdateEpcController.getUpdateEpcRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${MeesExemptionStep.ROUTE_SEGMENT}",
    )
