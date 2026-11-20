package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SummaryCardListFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsListStep

class SuperiorLandlordsListFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : SummaryCardListFormBasePage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${SuperiorLandlordsListStep.ROUTE_SEGMENT}",
    )
