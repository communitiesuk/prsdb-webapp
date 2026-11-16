package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationAlreadyRegisteredStep

class AlreadyRegisteredPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ReactivationAlreadyRegisteredStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val backLink = BackLink.default(page)
    val registerNewPropertyLink = Link(page.locator("#register-new-property-link"))
}
