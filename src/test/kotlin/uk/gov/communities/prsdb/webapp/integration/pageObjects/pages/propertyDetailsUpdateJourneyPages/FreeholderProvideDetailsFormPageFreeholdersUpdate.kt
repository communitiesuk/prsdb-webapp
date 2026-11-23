package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep

class FreeholderProvideDetailsFormPageFreeholdersUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        UpdateFreeholdersController.getUpdateFreeholdersRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ProvideFreeholderDetailsStep.ROUTE_SEGMENT}",
    ) {
    val backLink = BackLink.default(page)
}
