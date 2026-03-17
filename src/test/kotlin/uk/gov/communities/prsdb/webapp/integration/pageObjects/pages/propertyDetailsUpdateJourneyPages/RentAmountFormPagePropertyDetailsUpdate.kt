package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateRentFrequencyAndAmountController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.RentAmountFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RentAmountStep

class RentAmountFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : RentAmountFormBasePage(
        page,
        UpdateRentFrequencyAndAmountController.getUpdateRentFrequencyAndAmountRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${RentAmountStep.ROUTE_SEGMENT}",
    )
