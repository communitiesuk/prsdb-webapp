package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentFrequencyAndAmount.UpdateRentFrequencyAndAmountCyaStep

class CheckRentFrequencyAndAmountAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${UpdateRentFrequencyAndAmountCyaStep.ROUTE_SEGMENT}",
    )
