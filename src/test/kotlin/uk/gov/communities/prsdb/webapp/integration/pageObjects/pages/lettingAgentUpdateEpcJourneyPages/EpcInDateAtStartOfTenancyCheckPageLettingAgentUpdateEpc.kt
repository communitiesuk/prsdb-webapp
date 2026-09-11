package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateEpcJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateEpcController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.EpcInDateAtStartOfTenancyCheckBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import java.util.UUID

class EpcInDateAtStartOfTenancyCheckPageLettingAgentUpdateEpc(
    page: Page,
    urlArguments: Map<String, String>,
) : EpcInDateAtStartOfTenancyCheckBasePage(
        page,
        LettingAgentUpdateEpcController.getUpdateEpcRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT}",
    )
