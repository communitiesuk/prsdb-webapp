package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasEpcFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep

class HasEpcFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : HasEpcFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${HasEpcStep.ROUTE_SEGMENT}",
    )
