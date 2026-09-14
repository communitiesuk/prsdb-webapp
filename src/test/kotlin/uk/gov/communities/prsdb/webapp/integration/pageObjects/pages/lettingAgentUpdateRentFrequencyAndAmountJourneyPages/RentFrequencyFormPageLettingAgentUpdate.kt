package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentFrequencyAndAmountJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentFrequencyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep
import java.util.UUID

class RentFrequencyFormPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentFrequencyFormBasePage(
        page,
        LettingAgentUpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${RentFrequencyStep.ROUTE_SEGMENT}",
    )
