package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyReactivationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.ReactivatePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Heading
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.PostForm
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyReactivation.steps.ReactivationInfoStep

// TODO PDJB-2105: replace this placeholder with the real reactivation information page
class InfoFormPagePropertyReactivation(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        ReactivatePropertyController.getPropertyReactivationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ReactivationInfoStep.ROUTE_SEGMENT}",
    ) {
    val heading = Heading(page.locator("h1"))
    val backLink = BackLink.default(page)
    val form = PostForm(page)

    fun submit() = form.submit()
}
