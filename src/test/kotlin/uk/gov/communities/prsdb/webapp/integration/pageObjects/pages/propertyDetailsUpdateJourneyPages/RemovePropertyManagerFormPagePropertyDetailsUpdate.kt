package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdatePropertyManagerController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.RemovePropertyManagerStep

class RemovePropertyManagerFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        UpdatePropertyManagerController.getUpdatePropertyManagerRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${RemovePropertyManagerStep.ROUTE_SEGMENT}",
    )
