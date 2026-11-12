package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.HasGasSupplyFormBasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStep

class HasGasSupplyFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : HasGasSupplyFormBasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${HasGasSupplyStep.ROUTE_SEGMENT}",
    ) {
    val backLink = BackLink.default(page)
}
