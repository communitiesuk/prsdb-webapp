package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.leavePropertyJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.LeavePropertyController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class ConfirmationPageLeaveProperty(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        LeavePropertyController.getLeavePropertyBasePath(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val goToDashboardLink = Link.byText(page, "Go to dashboard")
}
