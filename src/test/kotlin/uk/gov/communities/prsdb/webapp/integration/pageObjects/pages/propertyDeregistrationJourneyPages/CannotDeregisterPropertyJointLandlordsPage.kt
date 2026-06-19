package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.CannotDeregisterStep

class CannotDeregisterPropertyJointLandlordsPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        DeregisterPropertyController.getPropertyDeregistrationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${CannotDeregisterStep.ROUTE_SEGMENT}",
    ) {
    val heading
        get() = page.locator("h1")

    val bodyText
        get() = page.locator("main .govuk-body")

    val noLongerALandlordLink = Link.byText(page, "I’m no longer a landlord for this property")
    val backLink = BackLink.default(page)
}
