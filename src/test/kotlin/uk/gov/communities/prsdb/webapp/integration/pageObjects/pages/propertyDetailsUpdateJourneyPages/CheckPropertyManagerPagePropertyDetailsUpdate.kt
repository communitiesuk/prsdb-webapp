package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdatePropertyManagerController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.CheckPropertyManagerFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckPropertyManagerStep

class CheckPropertyManagerPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : CheckPropertyManagerFormBasePage(
        page,
        UpdatePropertyManagerController.getUpdatePropertyManagerRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${CheckPropertyManagerStep.ROUTE_SEGMENT}",
    ) {
    fun confirm() = form.submit()
}
