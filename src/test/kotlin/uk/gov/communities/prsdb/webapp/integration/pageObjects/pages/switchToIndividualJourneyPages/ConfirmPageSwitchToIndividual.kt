package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.switchToIndividualJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Button
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage
import uk.gov.communities.prsdb.webapp.journeys.switchToIndividual.stepConfig.ConfirmOnlyLandlordStep

class ConfirmPageSwitchToIndividual(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        SwitchToIndividualController.getSwitchToIndividualBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/${ConfirmOnlyLandlordStep.ROUTE_SEGMENT}",
    ) {
    val confirmButton = Button(page.locator("button.govuk-button"))
    val cancelLink = Link.byText(page, "Cancel")

    fun submitConfirm() {
        confirmButton.clickAndWait()
    }
}
