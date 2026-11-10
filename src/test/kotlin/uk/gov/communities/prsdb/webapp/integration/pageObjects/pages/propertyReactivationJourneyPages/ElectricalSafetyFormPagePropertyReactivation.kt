package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationElectricalSafetyStep

// TODO PDJB-2039: replace this placeholder with the real reactivation electrical safety pages
class ElectricalSafetyFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ReactivationElectricalSafetyStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val backLink = BackLink.default(page)
    val form = PostForm(page)

    fun submit() = form.submit()
}
