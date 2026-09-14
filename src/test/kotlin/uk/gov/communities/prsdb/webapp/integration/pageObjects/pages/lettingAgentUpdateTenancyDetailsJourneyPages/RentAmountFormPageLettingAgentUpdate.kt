package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateTenancyDetailsJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateTenancyDetailsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentAmountFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import java.util.UUID

class RentAmountFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentAmountFormBasePage(
        page,
        LettingAgentUpdateTenancyDetailsController.getUpdateTenancyDetailsRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${RentAmountStep.ROUTE_SEGMENT}",
    )
