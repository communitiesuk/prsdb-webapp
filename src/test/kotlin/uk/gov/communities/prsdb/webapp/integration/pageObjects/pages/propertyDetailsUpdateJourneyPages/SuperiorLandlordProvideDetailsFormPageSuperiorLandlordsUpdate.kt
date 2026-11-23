package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDetailsUpdateJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.UpdateSuperiorLandlordsController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.PageWithYesNoRadios
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideSuperiorLandlordDetailsStep

class SuperiorLandlordProvideDetailsFormPageSuperiorLandlordsUpdate(
    page: Page,
    urlArguments: Map<String, String>,
) : PageWithYesNoRadios(
        page,
        UpdateSuperiorLandlordsController.getUpdateSuperiorLandlordsRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ProvideSuperiorLandlordDetailsStep.ROUTE_SEGMENT}",
    ) {
    val backLink = BackLink.default(page)
}
