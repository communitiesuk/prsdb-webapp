package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.propertyDeregistrationJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.DeregisterPropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.BackLink
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.propertyDeregistration.stepConfig.AreYouSureStep

class DeregisterPropertyInfoPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        DeregisterPropertyController.getPropertyDeregistrationBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${AreYouSureStep.ROUTE_SEGMENT}",
    ) {
    val heading
        get() = page.locator("h1")

    val bodyText
        get() = page.locator("main .govuk-body").first()

    val prnHeading
        get() = page.locator("main h2")

    val continueButton = Button(page.locator("button.govuk-button"))
    val cancelLink = Link.byText(page, "Cancel")
    val backLink = BackLink.default(page)

    fun submitContinue() {
        continueButton.clickAndWait()
    }
}
