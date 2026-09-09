package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.LandlordUpdateFurnishedStatusController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.FurnishedStatusFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FurnishedStatusStep

class FurnishedStatusFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : FurnishedStatusFormBasePage(
        page,
        LandlordUpdateFurnishedStatusController.getUpdateFurnishedStatusRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${FurnishedStatusStep.ROUTE_SEGMENT}",
    )
