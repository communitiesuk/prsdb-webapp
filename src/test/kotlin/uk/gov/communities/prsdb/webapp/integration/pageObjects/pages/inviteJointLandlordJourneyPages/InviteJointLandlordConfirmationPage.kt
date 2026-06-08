package uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.inviteJointLandlordJourneyPages

import com.microsoft.playwright.Page
import uk.gov.communities.prsdb.webapp.constants.CONFIRMATION_PATH_SEGMENT
import uk.gov.communities.prsdb.webapp.controllers.InviteJointLandlordController
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.ConfirmationBanner
import uk.gov.communities.prsdb.webapp.integration.pageObjects.components.Link
import uk.gov.communities.prsdb.webapp.integration.pageObjects.pages.basePages.BasePage

class InviteJointLandlordConfirmationPage(
    page: Page,
    urlArguments: Map<String, String>,
) : BasePage(
        page,
        InviteJointLandlordController.getInviteJointLandlordRoute(urlArguments["propertyOwnershipId"]!!.toLong()) +
            "/$CONFIRMATION_PATH_SEGMENT",
    ) {
    val confirmationBanner = ConfirmationBanner(page)
    val goBackToPropertyRecordLink = Link.byText(page, "Go back to the property record")
}
