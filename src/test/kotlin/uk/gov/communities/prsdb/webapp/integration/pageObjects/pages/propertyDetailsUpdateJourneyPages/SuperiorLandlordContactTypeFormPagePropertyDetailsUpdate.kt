package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateOwnershipController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.SuperiorLandlordContactTypeFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordContactTypeStep

class SuperiorLandlordContactTypeFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : SuperiorLandlordContactTypeFormBasePage(
        page,
        UpdateOwnershipController.getUpdateOwnershipRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${SuperiorLandlordContactTypeStep.ROUTE_SEGMENT}",
    )
