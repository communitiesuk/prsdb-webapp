package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckOccupancyAnswersFormPage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.rentFrequencyAndAmount.UpdateRentFrequencyAndAmountCyaStep

class CheckRentFrequencyAndAmountAnswersPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckOccupancyAnswersFormPage(
        page,
        LandlordUpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(
            urlArguments["propertyOwnershipId"]!!.toLong(),
        ) + "/${UpdateRentFrequencyAndAmountCyaStep.ROUTE_SEGMENT}",
    )
