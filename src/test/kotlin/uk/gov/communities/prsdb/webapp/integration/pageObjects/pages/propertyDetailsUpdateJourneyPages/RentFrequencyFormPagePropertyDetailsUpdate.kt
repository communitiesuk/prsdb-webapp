package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentFrequencyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentFrequencyStep

class RentFrequencyFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentFrequencyFormBasePage(
        page,
        LandlordUpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(
            urlArguments["propertyOwnershipId"]!!.toLong(),
        ) + "/${RentFrequencyStep.ROUTE_SEGMENT}",
    )
