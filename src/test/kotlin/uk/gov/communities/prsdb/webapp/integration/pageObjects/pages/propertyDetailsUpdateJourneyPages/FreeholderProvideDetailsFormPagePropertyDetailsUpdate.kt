package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateFreeholdersController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideFreeholderDetailsStep

class FreeholderProvideDetailsFormPagePropertyDetailsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        "${UpdateFreeholdersController.UPDATE_FREEHOLDERS_ROUTE.replace(
            "{propertyOwnershipId}",
            urlArguments["propertyOwnershipId"]!!,
        )}/${ProvideFreeholderDetailsStep.ROUTE_SEGMENT}",
    ) {
    val backLink = BackLink.default(page)
}
