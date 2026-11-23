package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdatePropertyManagerController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyManagerStep

class PropertyManagerFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        UpdatePropertyManagerController.getUpdatePropertyManagerRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${PropertyManagerStep.ROUTE_SEGMENT}",
    ) {
    val backLink = BackLink.default(page)
}
