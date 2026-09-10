package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentFrequencyAndAmountJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentAmountFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep
import java.util.UUID

class RentAmountFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentAmountFormBasePage(
        page,
        LettingAgentUpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${RentAmountStep.ROUTE_SEGMENT}",
    )
