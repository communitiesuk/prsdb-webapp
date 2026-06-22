package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.switchToIndividualJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.SwitchToIndividualController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageSwitchToIndividual(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        SwitchToIndividualController.getSwitchToIndividualBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val viewPropertyRecordLink = Link.byText(page, "View property record")
}
