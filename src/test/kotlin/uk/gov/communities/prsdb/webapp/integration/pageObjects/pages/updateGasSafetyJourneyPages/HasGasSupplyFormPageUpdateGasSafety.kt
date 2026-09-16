package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.updateGasSafetyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateGasSafetyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasGasSupplyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasSupplyStep

class HasGasSupplyFormPageUpdateGasSafety(
    page: Page,
    urlArguments: Map<String, String>,
) : HasGasSupplyFormBasePage(
        page,
        LandlordUpdateGasSafetyController.UPDATE_GAS_SAFETY_ROUTE
            .replace("{propertyOwnershipId}", urlArguments["propertyOwnershipId"]!!) +
            "/${BeforePdjb1022HasGasSupplyStep.ROUTE_SEGMENT}",
    )
