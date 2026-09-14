package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.lettingAgentUpdateRentFrequencyAndAmountJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LettingAgentUpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentFrequencyAndAmount.UpdateRentFrequencyAndAmountCyaStep
import java.util.UUID

class CheckRentFrequencyAndAmountAnswersPageLettingAgentUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        LettingAgentUpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(
            UUID.fromString(urlArguments["token"]!!),
        ) + "/${UpdateRentFrequencyAndAmountCyaStep.ROUTE_SEGMENT}",
    )
